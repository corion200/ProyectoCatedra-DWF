// =========================================================
// CFC Connect — app.js (JWT real + RBAC de UI + contraseñas temporales + catálogos)
// =========================================================
const API = '/api/v1';
let TOKEN = localStorage.getItem('cfcToken'),
    ROL   = localStorage.getItem('cfcRol'),
    USER  = localStorage.getItem('cfcUser');

const g = id => document.getElementById(id);

/** Matriz de permisos por rol (espejo de SecurityConfig + enunciado). */
const PERMISOS = {
    ADMIN:         ['agenda','cursos','clientes','inscripciones','pagos','usuarios','catalogos'],
    RECEPCIONISTA: ['agenda','cursos','clientes','inscripciones','usuarios'],
    CONTABILIDAD:  ['cursos','pagos'],
    CLIENTE:       ['cursos']
};
const PESTANA_INICIAL = { ADMIN:'agenda', RECEPCIONISTA:'agenda', CONTABILIDAD:'pagos', CLIENTE:'cursos' };

// ───────── HTTP helper con errores humanos ─────────
async function api(ruta, metodo='GET', cuerpo=null){
    const r = await fetch(API+ruta, { method:metodo,
        headers:{ 'Content-Type':'application/json', ...(TOKEN?{Authorization:'Bearer '+TOKEN}:{}) },
        body: cuerpo ? JSON.stringify(cuerpo) : null });
    if (r.ok) return r.status===204 ? null : r.json();
    if (r.status === 403) throw { status:403, message:'Tu rol ('+ROL+') no tiene permiso para esta operación.' };
    if (r.status === 401) throw { status:401, message:'Sesión no válida o expirada. Inicia sesión de nuevo.' };
    let msg = 'Error del servidor ('+r.status+')';
    try { const e = await r.json();
        msg = e.message + (e.detalles ? ' — '+Object.entries(e.detalles).map(([k,v])=>k+': '+v).join(' · ') : '');
    } catch(_) {}
    throw { status:r.status, message:msg };
}

function banner(txt,tipo){ const b=g('banner'); b.className='banner '+tipo; b.textContent=txt; }
function bannerLogin(txt,tipo){ const b=g('bannerLogin'); b.className='banner '+tipo; b.textContent=txt; }
function bannerPass(txt,tipo){ const b=g('bannerPass'); b.className='banner '+tipo; b.textContent=txt; }

// ───────── LOGIN / SESIÓN ─────────
async function login(){
    try{
        const t = await api('/auth/login','POST',
            { correo:g('logCorreo').value.trim(), password:g('logPass').value });
        setSesion(t);
    }catch(e){
        bannerLogin(e.status===401 ? 'Correo o contraseña incorrectos.' : e.message,'err');
    }
}
async function setSesion(t){
    TOKEN=t.token; ROL=t.rol; USER=t.correo;
    localStorage.setItem('cfcToken',TOKEN);
    localStorage.setItem('cfcRol',ROL);
    localStorage.setItem('cfcUser',USER);
    if (t.debeCambiarPassword) { abrirCambioPass(true); return; }
    iniciarApp();
}
function logout(){ localStorage.clear(); location.reload(); }

async function olvidePass(){
    try{
        const r = await api('/auth/recuperar?correo='+encodeURIComponent(g('logCorreo').value.trim()),'POST');
        bannerLogin('🔑 Contraseña temporal generada: '+r.contrasenaTemporal+' — úsela para entrar y deberá cambiarla.','ok');
    }catch(e){ bannerLogin(e.message,'err'); }
}

// ───────── CAMBIO DE CONTRASEÑA (modal) ─────────
function abrirCambioPass(obligatoria){
    g('modalPass').classList.remove('hidden');
    bannerPass('','');
    g('modalPassAviso').textContent = obligatoria
        ? 'Tu cuenta tiene una contraseña TEMPORAL generada por el sistema. Por seguridad debes establecer una nueva antes de continuar.'
        : 'Establece una nueva contraseña para tu cuenta.';
}
function cerrarCambioPass(){ g('modalPass').classList.add('hidden'); }
async function guardarPassword(){
    if (g('passNueva').value !== g('passNueva2').value) {
        bannerPass('Las nuevas contraseñas no coinciden.','err'); return;
    }
    try{
        await api('/auth/cambiar-password','PATCH',
            { passwordActual:g('passActual').value, passwordNueva:g('passNueva').value });
        g('modalPass').classList.add('hidden');
        localStorage.clear(); TOKEN=ROL=USER=null;
        bannerLogin('✅ Contraseña actualizada. Inicia sesión con tu nueva contraseña.','ok');
        g('logPass').value='';
    }catch(e){ bannerPass(e.message,'err'); }
}

// ───────── ARRANQUE / NAVEGACIÓN ─────────
function iniciarApp(){
    g('vistaLogin').classList.add('hidden');
    g('vistaApp').classList.remove('hidden');
    g('sesionInfo').innerHTML = 'Sesión: <b>'+USER+'</b><span class="rol-badge">'+ROL+'</span>';
    g('agendaFecha').value = new Date().toISOString().slice(0,10);

    const permitidas = PERMISOS[ROL] || [];
    document.querySelectorAll('nav .tab-btn').forEach(b=>{
        b.style.display = permitidas.includes(b.dataset.vista) ? '' : 'none';
    });
    if (ROL==='RECEPCIONISTA') g('notaRolSoloCliente').classList.remove('hidden');
    if (ROL!=='ADMIN') g('formCrearCurso').classList.add('hidden');

    const inicial = PESTANA_INICIAL[ROL] || 'cursos';
    tab(inicial, document.querySelector('.tab-btn[data-vista="'+inicial+'"]'));
}

function tab(id, btn){
    document.querySelectorAll('nav .tab-btn').forEach(b=>b.classList.remove('activo'));
    if (btn) btn.classList.add('activo');
    ['agenda','cursos','clientes','inscripciones','pagos','usuarios','catalogos']
        .forEach(s=>g(s).classList.toggle('hidden', s!==id));
    if (id==='agenda')        cargarAgenda();
    if (id==='cursos')      { cargarCursos();    if (ROL==='ADMIN') cargarOpcionesCurso(); }
    if (id==='clientes')      cargarClientes();
    if (id==='inscripciones') cargarOpcionesInscripcion();
    if (id==='pagos')         cargarOpcionesPago();
    if (id==='usuarios')      cargarUsuarios();
}

// ───────── AGENDA ─────────
async function cargarAgenda(){
    try{
        g('agendaTabla').innerHTML = '<table><tr><td colspan="5" class="cargando">Cargando</td></tr></table>';
        const items = await api('/agenda?fecha='+g('agendaFecha').value);
        g('agendaTabla').innerHTML = items.length
            ? '<table><thead><tr><th>Tipo</th><th>Actividad</th><th>Horario</th><th>Lugar</th><th>Estado</th></tr></thead><tbody>'
            + items.map(i=>'<tr><td><b>'+i.tipo+'</b></td><td>'+i.titulo+'</td><td>'
                +(i.horaInicio||'todo el día')+(i.horaFin?' – '+i.horaFin:'')+'</td><td>'+i.lugar
                +'</td><td>'+i.estado+'</td></tr>').join('')+'</tbody></table>'
            : '<p style="color:#6a748c">Sin actividades programadas para esta fecha.</p>';
    }catch(e){ g('agendaTabla').innerHTML = '<p style="color:var(--err)">'+e.message+'</p>'; }
}

// ───────── CURSOS ─────────
async function cargarCursos(){
    try{
        g('cursosTabla').innerHTML = '<table><tr><td colspan="7" class="cargando">Cargando</td></tr></table>';
        const pag = await api('/cursos?size=50');
        g('cursosTabla').innerHTML = pag.content.length
            ? '<table><thead><tr><th>Código</th><th>Nombre</th><th>Tipo</th><th>Precio</th><th>Cupo</th><th>Fechas</th><th>Estado</th></tr></thead><tbody>'
            + pag.content.map(c=>'<tr><td><b>'+c.codigo+'</b></td><td>'+c.nombre+'</td><td>'+c.tipo
                +'</td><td>$'+c.precio+'</td><td>'+c.cupoMaximo+'</td><td>'+c.fechaInicio+' → '+c.fechaFin
                +'</td><td>'+c.estado+'</td></tr>').join('')+'</tbody></table>'
            : '<p style="color:#6a748c">Aún no hay cursos registrados.</p>';
    }catch(e){ g('cursosTabla').innerHTML = '<p style="color:var(--err)">'+e.message+'</p>'; }
}
async function cargarOpcionesCurso(){
    try{
        const cats = await api('/categorias?size=50');
        g('curCategoria').innerHTML = cats.content.map(c=>`<option value="${c.id}">${c.nombre}</option>`).join('');
    }catch(e){ g('curCategoria').innerHTML = '<option value="">Error cargando categorías</option>'; }
    try{
        const mods = await api('/modalidades?size=50');
        g('curModalidad').innerHTML = mods.content.map(m=>`<option value="${m.id}">${m.nombre}</option>`).join('');
    }catch(e){ g('curModalidad').innerHTML = '<option value="">Error cargando modalidades</option>'; }
    try{
        const docs = await api('/docentes?size=50&activo=true');
        g('curDocente').innerHTML = docs.content.map(d=>`<option value="${d.id}">${d.nombres} ${d.apellidos}</option>`).join('');
    }catch(e){ g('curDocente').innerHTML = '<option value="">Error cargando docentes</option>'; }
}
async function crearCurso(){
    const v = id => g(id).value;
    if (!v('curCodigo') || !v('curNombre') || !v('curHoras') || !v('curPrecio')
        || !v('curInicio') || !v('curFin') || !v('curCategoria') || !v('curModalidad') || !v('curDocente')) {
        banner('❌ Completa todos los campos del curso (incluye categoría, modalidad y docente).','err'); return;
    }
    try{
        const c = await api('/cursos','POST',{
            codigo:v('curCodigo').trim().toUpperCase(), nombre:v('curNombre').trim(),
            descripcion:v('curDesc').trim()||null, tipo:v('curTipo'),
            categoriaId:+v('curCategoria'), modalidadId:+v('curModalidad'),
            horas:+v('curHoras'), precio:+v('curPrecio'), cupoMaximo:+v('curCupo'),
            fechaInicio:v('curInicio'), fechaFin:v('curFin'),
            docentesIds:[+v('curDocente')]
        });
        banner('✅ Curso creado: '+c.codigo+' — '+c.nombre+' (id '+c.id+')','ok');
        cargarCursos();
    }catch(e){ banner('❌ '+e.message,'err'); }
}

// ───────── CLIENTES ─────────
async function cargarClientes(){
    try{
        g('clientesTabla').innerHTML = '<table><tr><td colspan="7" class="cargando">Cargando</td></tr></table>';
        const pag = await api('/clientes?size=50');
        g('clientesTabla').innerHTML = pag.content.length
            ? '<table><thead><tr><th>ID</th><th>Tipo</th><th>Nombre</th><th>DUI</th><th>Correo</th><th>Teléfono</th><th>Estado</th></tr></thead><tbody>'
            + pag.content.map(c=>'<tr><td>'+c.id+'</td><td>'+c.tipoCliente+'</td><td>'+c.nombre+'</td><td>'
                +(c.dui||'—')+'</td><td>'+c.correo+'</td><td>'+c.telefono
                +'</td><td>'+(c.activo?'✅ Activo':'⛔ Inactivo')+'</td></tr>').join('')+'</tbody></table>'
            : '<p style="color:#6a748c">Aún no hay clientes registrados.</p>';
    }catch(e){ g('clientesTabla').innerHTML = '<p style="color:var(--err)">'+e.message+'</p>'; }
}
function tipoCliente(){
    const esEmpresa = g('cliTipo').value==='EMPRESA';
    g('campoDui').classList.toggle('hidden', esEmpresa);
    g('campoContacto').classList.toggle('hidden', !esEmpresa);
}
async function crearCliente(){
    const v = id => g(id).value;
    const esEmpresa = g('cliTipo').value==='EMPRESA';
    if (!v('cliNombre') || !v('cliNit') || !v('cliCorreo') || !v('cliTel')) {
        banner('❌ Completa nombre, NIT, correo y teléfono.','err'); return;
    }
    try{
        const c = await api('/clientes','POST',{
            tipoCliente:g('cliTipo').value, nombre:v('cliNombre').trim(),
            dui:esEmpresa?null:v('cliDui'), nit:v('cliNit'), correo:v('cliCorreo'),
            telefono:v('cliTel'), contactoNombre:esEmpresa?v('cliContacto'):null,
            direccion:v('cliDir')
        });
        banner('✅ Cliente creado: '+c.nombre+' (id '+c.id+')','ok');
        cargarClientes();
    }catch(e){ banner('❌ '+e.message,'err'); }
}

// ───────── INSCRIPCIONES ─────────
async function cargarOpcionesInscripcion(){
    try{
        const cli = await api('/clientes?size=100');
        g('insCliente').innerHTML = cli.content.length
            ? cli.content.map(c=>`<option value="${c.id}">${c.id} — ${c.nombre} (${c.tipoCliente})</option>`).join('')
            : '<option value="">No hay clientes registrados</option>';
    }catch(e){ g('insCliente').innerHTML = '<option value="">Error cargando clientes</option>'; }
    try{
        const cur = await api('/cursos?size=100');
        const programados = cur.content.filter(c=>c.estado==='PROGRAMADO');
        g('insCurso').innerHTML = programados.length
            ? programados.map(c=>`<option value="${c.id}">${c.codigo} — ${c.nombre} (cupo ${c.cupoMaximo})</option>`).join('')
            : '<option value="">No hay cursos programados</option>';
    }catch(e){ g('insCurso').innerHTML = '<option value="">Error cargando cursos</option>'; }
    listarInscripciones();
}
async function crearInscripcion(){
    if (!g('insCliente').value || !g('insCurso').value) {
        banner('❌ Selecciona un cliente y un curso.','err'); return;
    }
    try{
        const i = await api('/inscripciones','POST',
            { clienteId:+g('insCliente').value, cursoId:+g('insCurso').value });
        banner('✅ Inscripción '+i.codigo+' registrada ('+i.clienteNombre+' → '+i.cursoNombre+', estado '+i.estado+')','ok');
        listarInscripciones();
    }catch(e){ banner('❌ '+e.message,'err'); }
}
async function listarInscripciones(){
    try{
        const pag = await api('/inscripciones?size=20');
        g('insTabla').innerHTML = pag.content.length
            ? '<table><thead><tr><th>Código</th><th>Cliente</th><th>Curso</th><th>Estado</th></tr></thead><tbody>'
            + pag.content.map(i=>'<tr><td>'+i.codigo+'</td><td>'+i.clienteNombre+'</td><td>'+i.cursoNombre
                +'</td><td>'+i.estado+'</td></tr>').join('')+'</tbody></table>'
            : '<p style="color:#6a748c">Aún no hay inscripciones.</p>';
    }catch(e){}
}

// ───────── PAGOS ─────────
async function cargarOpcionesPago(){
    try{
        const ins = await api('/inscripciones?size=50');
        g('pagIns').innerHTML = ins.content.length
            ? ins.content.map(i=>`<option value="${i.id}">${i.codigo} — ${i.clienteNombre} (${i.estado})</option>`).join('')
            : '<option value="">No hay inscripciones registradas</option>';
    }catch(e){ g('pagIns').innerHTML = '<option value="">Error cargando inscripciones</option>'; }

    cargarPagosDeInscripcion();   // ⭐ <-- Agregado aquí
}
async function registrarPago(){
    if (!g('pagIns').value || !g('pagMonto').value) {
        banner('❌ Selecciona una inscripción y escribe el monto.','err'); return;
    }
    try{
        const p = await api('/pagos','POST',
            { inscripcionId:+g('pagIns').value, monto:+g('pagMonto').value, metodoPago:g('pagMetodo').value });
        banner('✅ Pago '+p.codigo+' registrado por $'+p.monto+' ('+p.metodoPago+')','ok');
        cargarPagosDeInscripcion(); // ⭐ Actualiza la tablita al registrar un pago nuevo
        verSaldo();                 // ⭐ Actualiza el saldo también
    }catch(e){ banner('❌ '+e.message,'err'); }
}
function animarSaldo(elemento, valorFinal){
    const objetivo = parseFloat(valorFinal), inicio = performance.now(), dur = 700;
    function paso(ahora){
        const progreso = Math.min((ahora-inicio)/dur, 1);
        const facil = 1 - Math.pow(1-progreso, 3);
        elemento.textContent = '$' + (objetivo*facil).toFixed(2);
        if (progreso < 1) requestAnimationFrame(paso);
    }
    requestAnimationFrame(paso);
}
async function verSaldo(){
    if (!g('pagIns').value) {
        g('saldoBox').innerHTML = '<p style="color:#b3453f">Selecciona primero una inscripción.</p>'; return;
    }
    try{
        const r = await api('/pagos/resumen/inscripcion/'+g('pagIns').value);
        g('saldoBox').innerHTML = '<p>Curso: <b>$'+r.precioCurso+'</b> · Pagado: <b>$'+r.totalPagado+'</b><br>'
            +'Saldo pendiente: <span class="saldo" id="saldoNum">$0.00</span></p>';
        animarSaldo(document.getElementById('saldoNum'), r.saldoPendiente);
    }catch(e){ g('saldoBox').innerHTML = '<p style="color:var(--err)">'+e.message+'</p>'; }

    cargarPagosDeInscripcion();   // ⭐ <-- Agregado aquí (Cambio 3)
}

// ───────── PAGOS: listado + validación de caja (cambio de estado) ─────────
async function cargarPagosDeInscripcion(){
    if (!g('pagIns').value) { g('pagosTabla').innerHTML=''; return; }
    try{
        const pag = await api('/pagos?inscripcionId='+g('pagIns').value+'&size=50');
        g('pagosTabla').innerHTML = pag.content.length
            ? '<table><thead><tr><th>Recibo</th><th>Monto</th><th>Método</th><th>Estado</th><th>Acción</th></tr></thead><tbody>'
            + pag.content.map(p=>{
                let accion = '';
                if (p.estado==='PENDIENTE')
                    accion = '<button class="primario" style="padding:6px 12px;margin:0" onclick="cambiarEstadoPago('+p.id+',\'PARCIAL\')">Validar (PARCIAL)</button>';
                else if (p.estado==='PARCIAL')
                    accion = '<button class="primario" style="padding:6px 12px;margin:0" onclick="cambiarEstadoPago('+p.id+',\'PAGADO\')">Liquidar (PAGADO)</button>';
                else accion = '<span style="color:var(--ok)">✔ Liquidado</span>';
                return '<tr><td><b>'+p.codigo+'</b></td><td>$'+p.monto+'</td><td>'+p.metodoPago
                    +'</td><td>'+p.estado+'</td><td>'+accion+'</td></tr>';
            }).join('')+'</tbody></table>'
            : '<p style="color:#6a748c">Esta inscripción aún no tiene pagos.</p>';
    }catch(e){ g('pagosTabla').innerHTML = '<p style="color:var(--err)">'+e.message+'</p>'; }
}
async function cambiarEstadoPago(id, nuevoEstado){
    try{
        const p = await api('/pagos/'+id+'/estado/'+nuevoEstado,'PATCH');
        banner('✅ Pago '+p.codigo+' actualizado a '+p.estado,'ok');
        cargarPagosDeInscripcion();
        verSaldo();   // ⭐ el saldo se recalcula al instante
    }catch(e){ banner('❌ '+e.message,'err'); }
}

// ───────── USUARIOS ─────────
async function cargarUsuarios(){
    if (ROL!=='ADMIN') {
        g('usuariosTabla').innerHTML = '<p style="color:#6a748c">El listado de usuarios está disponible solo para ADMIN. Puedes crear cuentas de rol CLIENTE más abajo.</p>';
        return;
    }
    try{
        g('usuariosTabla').innerHTML = '<table><tr><td colspan="5" class="cargando">Cargando</td></tr></table>';
        const lista = await api('/usuarios');
        g('usuariosTabla').innerHTML = '<table><thead><tr><th>Nombre</th><th>Correo</th><th>Rol</th><th>Estado</th><th>Cambio pendiente</th></tr></thead><tbody>'
            + lista.map(u=>'<tr><td>'+u.nombre+'</td><td>'+u.correo+'</td><td><b>'+u.rol+'</b></td><td>'
                +(u.activo?'✅':'⛔')+'</td><td>'+(u.debeCambiarPassword?'🔑 Sí':'—')+'</td></tr>').join('')+'</tbody></table>';
    }catch(e){ g('usuariosTabla').innerHTML = '<p style="color:var(--err)">'+e.message+'</p>'; }
}
async function crearUsuario(){
    if (!g('usuNombre').value || !g('usuCorreo').value) {
        banner('❌ Completa nombre y correo del usuario.','err'); return;
    }
    try{
        const r = await api('/usuarios','POST',
            { nombre:g('usuNombre').value.trim(), correo:g('usuCorreo').value.trim().toLowerCase(), rol:g('usuRol').value });
        g('temporalBox').classList.remove('hidden');
        g('temporalBox').innerHTML = '👤 <b>'+r.usuario.nombre+'</b> ('+r.usuario.correo+') creado con rol <b>'+r.usuario.rol
            +'</b>.<br>🔑 Contraseña temporal: <code>'+(r.contrasenaTemporal || '(revisa la consola del backend)')+'</code>'
        cargarUsuarios();
    }catch(e){ g('temporalBox').classList.add('hidden'); banner('❌ '+e.message,'err'); }
}

// ───────── CATÁLOGOS (solo ADMIN) ─────────
async function crearCategoria(){
    if (!g('catNombre').value) { banner('❌ Escribe el nombre de la categoría.','err'); return; }
    try{
        const c = await api('/categorias','POST',
            { nombre:g('catNombre').value.trim(), descripcion:g('catDesc').value.trim()||null });
        banner('✅ Categoría creada: '+c.nombre+' (id '+c.id+') — ya aparece en el formulario de Cursos','ok');
        g('catNombre').value=''; g('catDesc').value='';
    }catch(e){ banner('❌ '+e.message,'err'); }
}
async function crearModalidad(){
    if (!g('modNombre').value) { banner('❌ Escribe el nombre de la modalidad.','err'); return; }
    try{
        const m = await api('/modalidades','POST',
            { nombre:g('modNombre').value.trim(), descripcion:g('modDesc').value.trim()||null });
        banner('✅ Modalidad creada: '+m.nombre+' (id '+m.id+')','ok');
        g('modNombre').value=''; g('modDesc').value='';
    }catch(e){ banner('❌ '+e.message,'err'); }
}
async function crearDocente(){
    const v = id => g(id).value;
    if (!v('docNombres') || !v('docApellidos') || !v('docDui') || !v('docCorreo')) {
        banner('❌ Completa nombres, apellidos, DUI y correo.','err'); return;
    }
    try{
        const d = await api('/docentes','POST',{
            nombres:v('docNombres').trim(), apellidos:v('docApellidos').trim(),
            dui:v('docDui').trim(), nit:v('docNit').trim()||null,
            correo:v('docCorreo').trim().toLowerCase(), telefono:v('docTel').trim()||null,
            tituloProfesional:v('docTitulo').trim()||null, especialidad:v('docEsp').trim()||null
        });
        banner('✅ Docente creado: '+d.nombres+' '+d.apellidos+' (id '+d.id+')','ok');
        ['docNombres','docApellidos','docDui','docNit','docCorreo','docTel','docTitulo','docEsp']
            .forEach(id=>g(id).value='');
    }catch(e){ banner('❌ '+e.message,'err'); }
}

// ───────── AUTOLOGIN si hay token guardado ─────────
if (TOKEN) iniciarApp();