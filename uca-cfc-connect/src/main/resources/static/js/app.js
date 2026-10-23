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
    ADMIN:         ['agenda','cursos','clientes','inscripciones','espacios','catering','pagos','usuarios','catalogos'],
    RECEPCIONISTA: ['agenda','cursos','clientes','inscripciones','espacios','catering','usuarios'],
    CONTABILIDAD:  ['cursos','pagos'],
    CLIENTE:       ['cursos']
};
const PESTANA_INICIAL = { ADMIN:'agenda', RECEPCIONISTA:'agenda', CONTABILIDAD:'pagos', CLIENTE:'cursos' };
const puede   = sec => (PERMISOS[ROL]||[]).includes(sec);
const esAdmin = () => ROL==='ADMIN';

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
    localStorage.setItem('cfcDebeCambiar', t.debeCambiarPassword ? '1' : '0');
    iniciarApp();                                     // entra a la app siempre…
    if (t.debeCambiarPassword) abrirCambioPass(true); // …y si la contraseña es temporal, el modal encima
}
function logout(){ localStorage.clear(); location.reload(); }

// ───────── CAMBIO DE CONTRASEÑA (modal) ─────────
function abrirCambioPass(obligatoria){
    g('modalPass').classList.remove('hidden');
    bannerPass('','');
    g('modalPassAviso').textContent = obligatoria
        ? 'Tu cuenta tiene una contraseña TEMPORAL generada por el sistema. Por seguridad debes establecer una nueva antes de continuar.'
        : 'Establece una nueva contraseña para tu cuenta.';
    setTimeout(() => g('passActual').focus(), 80);
}
function cerrarCambioPass(){
    g('modalPass').classList.add('hidden');
    bannerPass('','');
    if (localStorage.getItem('cfcDebeCambiar') === '1')
        banner('⚠️ Sigues usando una contraseña temporal. Puedes cambiarla cuando quieras con el botón “🔒 Mi contraseña”.','err');
}
async function guardarPassword(){
    const actual = g('passActual').value, nueva = g('passNueva').value, nueva2 = g('passNueva2').value;
    // Validación en el cliente: nunca enviar vacíos al backend (evita el 400 feo)
    if (!actual || !nueva || !nueva2) { bannerPass('Completa los tres campos antes de guardar.','err'); return; }
    if (nueva.length < 8) { bannerPass('La nueva contraseña debe tener al menos 8 caracteres.','err'); return; }
    if (nueva !== nueva2) { bannerPass('Las nuevas contraseñas no coinciden.','err'); return; }
    if (nueva === actual) { bannerPass('La nueva contraseña debe ser distinta de la actual.','err'); return; }
    try{
        await api('/auth/cambiar-password','PATCH',{ passwordActual:actual, passwordNueva:nueva });
        g('modalPass').classList.add('hidden');
        localStorage.clear(); TOKEN=ROL=USER=null;
        g('vistaApp').classList.add('hidden');
        g('vistaLogin').classList.remove('hidden');
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
    g('alqFecha').value    = new Date().toISOString().slice(0,10);
    g('catFecha').value    = new Date().toISOString().slice(0,10);

    aplicarPermisosUI();

    const inicial = PESTANA_INICIAL[ROL] || 'cursos';
    tab(inicial, document.querySelector('.tab-btn[data-vista="'+inicial+'"]'));
}

/** Oculta TODO lo que el rol no puede usar: pestañas del nav + bloques marcados con data-rol. */
function aplicarPermisosUI(){
    // 1) Pestañas del nav: solo las secciones permitidas al rol
    document.querySelectorAll('nav .tab-btn').forEach(b=>{
        b.style.display = puede(b.dataset.vista) ? '' : 'none';
    });
    // 2) Bloques/acciones restringidas: data-rol="ADMIN" o lista tipo data-rol="ADMIN,CONTABILIDAD"
    document.querySelectorAll('[data-rol]').forEach(el=>{
        const roles = el.dataset.rol.split(',').map(r=>r.trim());
        el.classList.toggle('hidden', !roles.includes(ROL));
    });
    // 3) En Usuarios: la recepcionista solo puede crear cuentas de rol CLIENTE
    const selRol = g('usuRol');
    if (selRol){
        selRol.innerHTML = ROL==='ADMIN'
            ? '<option>CLIENTE</option><option>RECEPCIONISTA</option><option>CONTABILIDAD</option><option>ADMIN</option>'
            : '<option>CLIENTE</option>';
    }
    if (ROL==='RECEPCIONISTA') g('notaRolSoloCliente')?.classList.remove('hidden');
}

function tab(id, btn){
    if (!puede(id)) return; // defensa extra: nunca entrar a una sección no permitida al rol
    document.querySelectorAll('nav .tab-btn').forEach(b=>b.classList.remove('activo'));
    if (btn) btn.classList.add('activo');
    ['agenda','cursos','clientes','inscripciones','espacios','catering','pagos','usuarios','catalogos']
        .forEach(s=>g(s).classList.toggle('hidden', s!==id));
    if (id==='agenda')        cargarAgenda();
    if (id==='cursos')      { cargarCursos();    if (ROL==='ADMIN') cargarOpcionesCurso(); }
    if (id==='clientes')      cargarClientes();
    if (id==='inscripciones') cargarOpcionesInscripcion();
    if (id==='espacios')      cargarVistaEspacios();
    if (id==='catering')      cargarVistaCatering();
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
        paginarVista('agendaTabla');
    }catch(e){ g('agendaTabla').innerHTML = '<p style="color:var(--err)">'+e.message+'</p>'; }
}

// ───────── CURSOS ─────────
async function cargarCursos(){
    try{
        g('cursosTabla').innerHTML = '<table><tr><td colspan="7" class="cargando">Cargando</td></tr></table>';
        const lista = await cargarTodo('/cursos');
        g('cursosTabla').innerHTML = lista.length
            ? '<table><thead><tr><th>Código</th><th>Nombre</th><th>Tipo</th><th>Precio</th><th>Cupo</th><th>Fechas</th><th>Estado</th></tr></thead><tbody>'
            + lista.map(c=>'<tr><td><b>'+c.codigo+'</b></td><td>'+c.nombre+'</td><td>'+c.tipo
                +'</td><td>$'+c.precio+'</td><td>'+c.cupoMaximo+'</td><td>'+c.fechaInicio+' → '+c.fechaFin
                +'</td><td>'+c.estado+'</td></tr>').join('')+'</tbody></table>'
            : '<p style="color:#6a748c">Aún no hay cursos registrados.</p>';
        filtrarTabla('filtroCursos','cursosTabla');
    }catch(e){ g('cursosTabla').innerHTML = '<p style="color:var(--err)">'+e.message+'</p>'; }
}
async function cargarOpcionesCurso(){
    try{
        const cats = await cargarTodo('/categorias');
        g('curCategoria').innerHTML = cats.map(c=>`<option value="${c.id}">${c.nombre}</option>`).join('');
    }catch(e){ g('curCategoria').innerHTML = '<option value="">Error cargando categorías</option>'; }
    try{
        const mods = await cargarTodo('/modalidades');
        g('curModalidad').innerHTML = mods.map(m=>`<option value="${m.id}">${m.nombre}</option>`).join('');
    }catch(e){ g('curModalidad').innerHTML = '<option value="">Error cargando modalidades</option>'; }
    try{
        const docs = await cargarTodo('/docentes','activo=true');
        g('curDocente').innerHTML = docs.map(d=>`<option value="${d.id}">${d.nombres} ${d.apellidos}</option>`).join('');
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
        const lista = await cargarTodo('/clientes');
        g('clientesTabla').innerHTML = lista.length
            ? '<table><thead><tr><th>ID</th><th>Tipo</th><th>Nombre</th><th>DUI</th><th>Correo</th><th>Teléfono</th><th>Estado</th></tr></thead><tbody>'
            + lista.map(c=>'<tr><td>'+c.id+'</td><td>'+c.tipoCliente+'</td><td>'+c.nombre+'</td><td>'
                +(c.dui||'—')+'</td><td>'+c.correo+'</td><td>'+c.telefono
                +'</td><td>'+(c.activo?'✅ Activo':'⛔ Inactivo')+'</td></tr>').join('')+'</tbody></table>'
            : '<p style="color:#6a748c">Aún no hay clientes registrados.</p>';
        filtrarTabla('filtroClientes','clientesTabla');
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
        cargarClientesCache();   // refresca también los buscadores de cliente
    }catch(e){ banner('❌ '+e.message,'err'); }
}

// ───────── INSCRIPCIONES ─────────
async function cargarOpcionesInscripcion(){
    await cargarClientesCache();
    try{
        const cur = await cargarTodo('/cursos');
        actualizarBuscador('insCurso', cur.filter(c=>c.estado==='PROGRAMADO'));
    }catch(e){ actualizarBuscador('insCurso', []); }
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
        const lista = await cargarTodo('/inscripciones');
        g('insTabla').innerHTML = lista.length
            ? '<table><thead><tr><th>Código</th><th>Cliente</th><th>Curso</th><th>Estado</th></tr></thead><tbody>'
            + lista.map(i=>'<tr><td>'+i.codigo+'</td><td>'+i.clienteNombre+'</td><td>'+i.cursoNombre
                +'</td><td>'+i.estado+'</td></tr>').join('')+'</tbody></table>'
            : '<p style="color:#6a748c">Aún no hay inscripciones.</p>';
        paginarVista('insTabla');
    }catch(e){}
}

// ───────── ESPACIOS / ALQUILERES (venue) ─────────
/* ⚠️ Si tus controllers usan otras rutas, cámbialas SOLO aquí.
   Espacios y alquileres están confirmados; la de catering se AUTO-DETECTA (ver rutaCatering). */
const RUTAS = { espacios:'/espacios', alquileres:'/alquileres' };

/* ── Ruta de catering auto-detectada ──
   El backend NO expone /catering-servicios (error NoResourceFoundException en el log).
   Probamos los candidatos típicos contra el backend real y usamos el primero que responda.
   Si sabés el @RequestMapping exacto de CateringController, ponelo en RUTA_CATERING_FIJA y listo. */
const CANDIDATOS_CATERING = ['/catering','/caterings','/catering-servicios','/servicios-catering','/catering-reservas'];
const RUTA_CATERING_FIJA = '/caterings';   // confirmado: @RequestMapping("/api/v1/caterings")
let RUTA_CATERING = null;
async function rutaCatering(){
    if (RUTA_CATERING) return RUTA_CATERING;
    if (RUTA_CATERING_FIJA) return (RUTA_CATERING = RUTA_CATERING_FIJA);
    for (const cand of CANDIDATOS_CATERING){
        try{
            await api(cand+'?size=1');
            RUTA_CATERING = cand;
            console.info('[CFC] Ruta de catering detectada: '+cand);
            return RUTA_CATERING;
        }catch(e){ /* probamos el siguiente candidato */ }
    }
    RUTA_CATERING = '/catering';   // último intento: deja que el backend muestre su error real
    return RUTA_CATERING;
}

/* Acepta Page de Spring ({content:[...]}) o array plano */
const datosDe = r => Array.isArray(r) ? r : (r?.content || []);

/* ═══ Carga TODAS las páginas que expone el backend (Spring Page) ═══
   Los listados ya no dependen del ?size= del primer pedido: se traen
   páginas de 200 hasta la última (tope de seguridad: 25 páginas = 5.000
   registros) y el paginador de la UI se encarga de mostrarlos cómodos. */
const PAG_BACKEND = 200, MAX_PAGINAS_BACKEND = 25;
async function cargarTodo(ruta, extra=''){
    let page = 0, out = [], totalPages = 1;
    do{
        const r = await api(ruta+'?page='+page+'&size='+PAG_BACKEND+(extra?'&'+extra:''));
        const arr = datosDe(r);
        out = out.concat(arr);
        if (Array.isArray(r) || !arr.length) break;      // array plano o fin inesperado
        totalPages = r.totalPages || 1;
        page++;
    } while (page < Math.min(totalPages, MAX_PAGINAS_BACKEND));
    return out;
}
const nomEspacio = a => a.espacioNombre || a.espacio?.nombre || a.espacio?.codigo || ('Espacio #'+(a.espacioId ?? '—'));
const nomCliente = a => a.clienteNombre || a.cliente?.nombre || ('Cliente #'+(a.clienteId ?? '—'));

/* ═════════ BUSCADORES CON FILTRO + FILTROS DE TABLA (retro de la docente) ═════════
   Los selects largos se reemplazan por un buscador: escribes y filtra al instante.
   Cada buscador guarda el id elegido en un input hidden con el MISMO id que tenía el select. */
const BUSQ = {};                       // id -> estado del buscador
let CLIENTES_CACHE = [];

const normTxt = s => (s||'').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g,'');

function crearBuscador(id, placeholder, etiqueta, onSel){
    const hidden = g(id);
    if (!hidden) return;
    const wrap = document.createElement('div'); wrap.className = 'selector';
    hidden.parentNode.insertBefore(wrap, hidden);
    wrap.appendChild(hidden);
    const inp = document.createElement('input');
    inp.type = 'text'; inp.id = id+'Busq'; inp.placeholder = placeholder; inp.autocomplete = 'off';
    const lista = document.createElement('div'); lista.className = 'resultados';
    wrap.append(inp, lista);
    BUSQ[id] = { items:[], etiqueta, onSel, inp, lista, hidden };
    inp.addEventListener('focus', ()=>pintarBuscador(id));
    inp.addEventListener('input', ()=>{ hidden.value=''; inp.classList.remove('incompleto'); pintarBuscador(id); });
    inp.addEventListener('keydown', e=>{
        if (e.key==='Escape'){ lista.classList.remove('abierto'); return; }
        if (e.key!=='Enter') return;
        e.preventDefault();                       // Enter = elegir: id exacto o 1.ª sugerencia
        const st = BUSQ[id], txt = inp.value.trim();
        const exacto = st.items.find(x=>String(x.id)===txt);
        if (exacto){ seleccionarBuscador(id, exacto.id); return; }
        const q = normTxt(txt);
        const res = st.items.filter(it=>!q || normTxt(st.etiqueta(it)).includes(q));
        if (res.length) seleccionarBuscador(id, res[0].id);
    });
    /* FIX “eso no debería pasar”: al salir del campo resolvemos lo tecleado aunque no
       se haya hecho clic en una sugerencia — escribir "3" SÍ elige al cliente id 3. */
    inp.addEventListener('blur', ()=>{ intentarAutoSeleccionar(id); setTimeout(()=>lista.classList.remove('abierto'), 150); });
    lista.addEventListener('mousedown', e=>{
        const item = e.target.closest('.res');
        if (item && item.dataset.id) seleccionarBuscador(id, item.dataset.id);
    });
}
function pintarBuscador(id){
    const st = BUSQ[id];
    const q = normTxt(st.inp.value);
    const res = st.items.filter(it => !q || normTxt(st.etiqueta(it)).includes(q)).slice(0,15);
    st.lista.innerHTML = res.length
        ? res.map(it=>'<div class="res" data-id="'+it.id+'">'+st.etiqueta(it)+'</div>').join('')
        : '<div class="res vacia">'+(st.items.length ? 'Sin coincidencias' : 'Sin datos disponibles')+'</div>';
    st.lista.classList.add('abierto');
}
function seleccionarBuscador(id, valor){
    const st = BUSQ[id];
    const it = st.items.find(x=>String(x.id)===String(valor));
    if (!it) return;
    st.hidden.value = it.id;
    st.inp.value = st.etiqueta(it);
    st.inp.classList.remove('incompleto');
    st.lista.classList.remove('abierto');
    if (st.onSel) st.onSel(it);
}
/** Resuelve lo tecleado sin clic: 1) id exacto · 2) etiqueta exacta · 3) única coincidencia.
 Si no coincide con nadie, marca el campo en rojo para avisar ANTES de guardar. */
function intentarAutoSeleccionar(id){
    const st = BUSQ[id];
    if (!st) return false;
    if (st.hidden.value){ st.inp.classList.remove('incompleto'); return true; }
    const txt = st.inp.value.trim();
    if (!txt) return false;
    const q = normTxt(txt);
    let it = st.items.find(x=>String(x.id)===txt);
    if (!it){ const ex = st.items.filter(x=>normTxt(st.etiqueta(x))===q);
        if (ex.length===1) it = ex[0]; }
    if (!it){ const par = st.items.filter(x=>normTxt(st.etiqueta(x)).includes(q));
        if (par.length===1) it = par[0]; }
    if (it){ seleccionarBuscador(id, it.id); return true; }
    st.inp.classList.add('incompleto');
    return false;
}
function actualizarBuscador(id, items){
    if (!BUSQ[id]) return;
    BUSQ[id].items = items || [];
    BUSQ[id].hidden.value = '';
    BUSQ[id].inp.value = '';
}
/* ═════════ PAGINACIÓN CLIENTE (preparada para muchos registros) ═════════
   - Todas las tablas se parten en páginas de 10/25/50/100 filas.
   - El filtrado (texto + desplegables) se aplica PRIMERO y el paginador
     pagina el conjunto filtrado; al filtrar se vuelve a la página 1.
   - El paginador se genera solo debajo de cada tabla (id: pag_<tablaId>). */
const PAG_UI = {};                                   // tablaId -> {pagina, tamano, inputId, filas}
const PAG_TAMANO_DEF = 10;

function pagEstado(tablaId){
    return PAG_UI[tablaId] ??= { pagina:1, tamano:PAG_TAMANO_DEF, inputId:null, filas:[] };
}
function filasDeTabla(tablaId){
    const cont = g(tablaId);
    return cont ? [...cont.querySelectorAll('table tbody tr')] : [];
}
/** Recalcula qué filas pasan los filtros y dibuja la página actual. */
function refrescarTabla(tablaId, inputId){
    const tabla = g(tablaId);
    if (!tabla) return;
    const st = pagEstado(tablaId);
    if (inputId) st.inputId = inputId;
    poblarFiltros(tablaId);            // llena/actualiza los desplegables (conserva la opción elegida)
    const inp = st.inputId ? g(st.inputId) : null;
    const q = inp ? normTxt(inp.value) : '';
    const sels = [...document.querySelectorAll('select.filtroSel[data-tabla="'+tablaId+'"]')];
    st.filas = [];
    tabla.querySelectorAll('tbody tr').forEach(tr=>{
        let mostrar = !q || normTxt(tr.textContent).includes(q);
        for (const sel of sels){
            if (!mostrar || !sel.value) continue;
            const celda = tr.children[+sel.dataset.col];
            if (!celda || !normTxt(celda.textContent).includes(normTxt(sel.value))) { mostrar = false; break; }
        }
        if (mostrar) st.filas.push(tr);
    });
    const maxPag = Math.max(1, Math.ceil(st.filas.length / st.tamano));
    if (st.pagina > maxPag) st.pagina = maxPag;
    if (st.pagina < 1)      st.pagina = 1;
    dibujarPagina(tablaId);
}
/** Compatibilidad: los oninput/onchange del HTML siguen llamando filtrarTabla(...). */
function filtrarTabla(inputId, tablaId){
    pagEstado(tablaId).pagina = 1;     // filtro nuevo → siempre vuelve a la página 1
    refrescarTabla(tablaId, inputId);
}
/** Para tablas SIN barra de filtros (agenda, inscripciones, pagos, usuarios). */
function paginarVista(tablaId){ refrescarTabla(tablaId); }
function pagIr(tablaId, p){
    pagEstado(tablaId).pagina = p;
    refrescarTabla(tablaId);
    g(tablaId)?.scrollIntoView({behavior:'smooth', block:'nearest'});
}
function pagTamano(tablaId, sel){
    const st = pagEstado(tablaId);
    st.tamano = Math.max(1, +sel.value || PAG_TAMANO_DEF);
    st.pagina = 1;
    refrescarTabla(tablaId);
}
/** Números de página con ventana móvil: 1 … 4 5 6 … 23 */
function pagBotones(maxPag, actual){
    const out = [], ini = Math.max(1, actual-2), fin = Math.min(maxPag, actual+2);
    if (ini > 1){ out.push(1); if (ini > 2) out.push('…'); }
    for (let p=ini; p<=fin; p++) out.push(p);
    if (fin < maxPag){ if (fin < maxPag-1) out.push('…'); out.push(maxPag); }
    return out;
}
function dibujarPagina(tablaId){
    const st = pagEstado(tablaId), cont = g(tablaId);
    if (!cont) return;
    let caja = g('pag_'+tablaId);
    if (!caja){
        caja = document.createElement('div');
        caja.id = 'pag_'+tablaId; caja.className = 'paginacion';
        cont.appendChild(caja);
    }
    const hayTabla = cont.querySelector('table'), cargando = cont.querySelector('.cargando');
    const total = st.filas.length;
    if (!hayTabla || cargando || total===0){ caja.style.display='none'; return; }
    const maxPag = Math.max(1, Math.ceil(total / st.tamano));
    const ini = (st.pagina-1)*st.tamano, fin = Math.min(ini+st.tamano, total);

    filasDeTabla(tablaId).forEach(tr=>{ tr.style.display='none'; });        // oculta TODO
    st.filas.slice(ini, fin).forEach(tr=>{ tr.style.display=''; });         // muestra solo la página

    let html = '<span class="info">Mostrando <b>'+(ini+1)+'–'+fin+'</b> de <b>'+total+'</b> registro'+(total===1?'':'s')
        +(st.inputId && g(st.inputId)?.value ? ' (filtrados)' : '')+'</span>';
    if (maxPag > 1){
        html += '<button '+(st.pagina===1?'disabled':'')+' onclick="pagIr(\''+tablaId+'\',1)" title="Primera página">«</button>'
            +   '<button '+(st.pagina===1?'disabled':'')+' onclick="pagIr(\''+tablaId+'\','+(st.pagina-1)+')" title="Anterior">‹</button>'
            +   pagBotones(maxPag, st.pagina).map(p=> p==='…'
                ? '<span class="puntos">…</span>'
                : '<button class="'+(p===st.pagina?'activo':'')+'" onclick="pagIr(\''+tablaId+'\','+p+')">'+p+'</button>').join('')
            +   '<button '+(st.pagina===maxPag?'disabled':'')+' onclick="pagIr(\''+tablaId+'\','+(st.pagina+1)+')" title="Siguiente">›</button>'
            +   '<button '+(st.pagina===maxPag?'disabled':'')+' onclick="pagIr(\''+tablaId+'\','+maxPag+')" title="Última página">»</button>';
    }
    html += '<span class="tamano-pag">Por página <select onchange="pagTamano(\''+tablaId+'\',this)">'
        + [10,25,50,100].map(n=>'<option '+(n===st.tamano?'selected':'')+'>'+n+'</option>').join('')
        + '</select></span>';
    caja.innerHTML = html;
}
/** Llena cada select .filtroSel con opciones fijas (data-opciones) + valores únicos de su columna (data-col). */
function poblarFiltros(tablaId){
    const tabla = g(tablaId);
    if (!tabla) return;
    const filas = [...tabla.querySelectorAll('tbody tr')];
    const limpio = v => v.replace(/^[^\w$ÁÉÍÓÚÑáéíóúñ]+/,'').trim() || v; // quita emojis (✅, ⛔…) del label
    document.querySelectorAll('select.filtroSel[data-tabla="'+tablaId+'"]').forEach(sel=>{
        const col = +sel.dataset.col, actual = sel.value;
        const fijas = (sel.dataset.opciones||'').split(',').map(s=>s.trim()).filter(Boolean);
        const deDatos = filas.map(tr=>tr.children[col]?.textContent.trim()).filter(Boolean).map(limpio).filter(Boolean);
        const valores = [...new Set([...fijas, ...deDatos])];
        sel.innerHTML = '<option value="">'+(sel.dataset.todos||'Todos')+'</option>'
            + valores.map(v=>'<option value="'+v+'">'+v+'</option>').join('');
        if (valores.includes(actual)) sel.value = actual;   // no pierdas lo que el usuario ya filtró
    });
}

async function cargarVistaEspacios(){
    await cargarEspacios();        // tabla + buscador de espacios
    await cargarClientesCache();   // buscador de clientes
    cargarAlquileres();
}
async function cargarVistaCatering(){
    cargarCatering();
    cargarClientesCache();
    cargarLugaresCatering();
}

/* Llena el desplegable de lugar con los ESPACIOS reales de la BD + opción "Otro…" (catering a domicilio) */
async function cargarLugaresCatering(){
    const sel = g('catLugar');
    if (!sel) return;
    let opciones = '';
    try{
        const espacios = await cargarTodo(RUTAS.espacios);
        espacios.forEach(e=>{
            opciones += '<option value="'+(e.nombre||e.codigo)+'">'+(e.nombre||e.codigo)
                +' — '+(e.tipo||'')+(e.capacidad?' (cap. '+e.capacidad+')':'')+'</option>';
        });
    }catch(e){ /* sin espacios cargables: queda solo la opción Otro */ }
    sel.innerHTML = opciones + '<option value="__otro">Otro lugar (escribir)…</option>';
    const otro = g('catLugarOtro');
    sel.onchange = ()=> otro && otro.classList.toggle('hidden', sel.value!=='__otro');
}

/* Carga la lista de clientes UNA vez y alimenta todos los buscadores de cliente */
async function cargarClientesCache(){
    try{
        CLIENTES_CACHE = await cargarTodo('/clientes');
        ['insCliente','alqCliente','catCliente'].forEach(id=>actualizarBuscador(id, CLIENTES_CACHE));
    }catch(e){ /* los buscadores quedan con lo que tengan */ }
}

async function cargarEspacios(){
    try{
        g('espaciosTabla').innerHTML = '<table><tr><td colspan="6" class="cargando">Cargando</td></tr></table>';
        const lista = await cargarTodo(RUTAS.espacios);
        // El campo del costo/hora cambia de nombre según el DTO del backend: probamos los habituales
        const costoDe = e => e.costoHora ?? e.precioHora ?? e.costoPorHora ?? e.tarifaHora ?? e.precioPorHora;
        g('espaciosTabla').innerHTML = lista.length
            ? '<table><thead><tr><th>Código</th><th>Nombre</th><th>Tipo</th><th>Capacidad</th><th>Costo/hora</th><th>Estado</th></tr></thead><tbody>'
            + lista.map(e=>{
                const ch = costoDe(e);
                return '<tr><td><b>'+(e.codigo||e.id)+'</b></td><td>'+(e.nombre||'—')+'</td><td>'+(e.tipo||'—')
                    +'</td><td>'+(e.capacidad??'—')+'</td><td>'+(ch!=null?'$'+ch:'—')
                    +'</td><td>'+(e.activo===false?'⛔ Inactivo':'✅ Activo')+'</td></tr>';
            }).join('')+'</tbody></table>'
            : '<p style="color:#6a748c">Aún no hay espacios registrados.</p>';
        actualizarBuscador('alqEspacio', lista);
        filtrarTabla('filtroEspacios','espaciosTabla');
    }catch(e){
        g('espaciosTabla').innerHTML = '<p style="color:var(--err)">'+e.message+'</p>';
        actualizarBuscador('alqEspacio', []);
    }
}

async function crearEspacio(){
    if (!esAdmin()) { banner('❌ Solo un ADMIN puede registrar espacios.','err'); return; }
    const v = id => g(id).value;
    if (!v('espCodigo') || !v('espNombre') || !v('espTipo') || !v('espCapacidad') || !v('espCostoHora')) {
        banner('❌ Completa código, nombre, tipo, capacidad y costo por hora.','err'); return;
    }
    try{
        const nuevo = await api(RUTAS.espacios,'POST',{
            codigo:v('espCodigo').trim().toUpperCase(), nombre:v('espNombre').trim(),
            tipo:v('espTipo'), capacidad:+v('espCapacidad'),
            costoHora:+v('espCostoHora'), precioHora:+v('espCostoHora')
        });
        banner('✅ Espacio creado: '+(nuevo.codigo||nuevo.id)+' — '+(nuevo.nombre||''),'ok');
        ['espCodigo','espNombre','espCapacidad','espCostoHora'].forEach(id=>g(id).value='');
        cargarEspacios();
    }catch(e){ banner('❌ '+e.message,'err'); }
}

async function crearAlquiler(){
    const v = id => g(id).value;
    const faltan = [];
    if (!v('alqEspacio')) faltan.push('espacio (escribe y elige una sugerencia)');
    if (!v('alqCliente')) faltan.push('cliente (escribe y elige una sugerencia)');
    if (!v('alqFecha'))   faltan.push('fecha');
    if (!v('alqInicio'))  faltan.push('hora de inicio');
    if (!v('alqFin'))     faltan.push('hora de fin');
    if (faltan.length){ banner('❌ Falta completar: '+faltan.join(' · ')+'.','err'); return; }
    if (v('alqFin') <= v('alqInicio')) {
        banner('❌ La hora fin debe ser posterior a la hora inicio.','err'); return;
    }
    try{
        const a = await api(RUTAS.alquileres,'POST',{
            espacioId:+v('alqEspacio'), clienteId:+v('alqCliente'),
            fechaEvento:v('alqFecha'), fecha:v('alqFecha'),   // el DTO espera fechaEvento
            horaInicio:v('alqInicio'), horaFin:v('alqFin'),
            observaciones:v('alqObs').trim()||null
        });
        banner('✅ Reserva registrada: '+nomEspacio(a)+' el '+(a.fechaEvento||a.fecha||v('alqFecha'))
            +' ('+(a.horaInicio||v('alqInicio'))+'–'+(a.horaFin||v('alqFin'))+')'
            +(a.costoTotal!=null?' — Costo: $'+a.costoTotal:''),'ok');
        ['alqObs'].forEach(id=>g(id).value='');
        cargarAlquileres();
    }catch(e){ banner('❌ '+e.message,'err'); }
}

/* ── Gestión del ciclo de vida (confirmar / rechazar / finalizar) ──
   ADMIN y RECEPCIONISTA gestionan el estado de reservas y caterings desde la tabla.
   Mappings CONFIRMADOS del backend (AlquilerController):
     PATCH /alquileres/{id}/confirmar · /{id}/finalizar · /{id}/cancelar?motivo=…
   CateringServiceImpl tiene los mismos métodos; si su Controller usa otro mapping,
   el autodetectar de abajo lo encuentra igual (y memoriza el que funcione). */
const ACCION_ESTADO = {
    alquileres: { confirmar:'CONFIRMADA', cancelar:'CANCELADA', finalizar:'FINALIZADA' },
    catering:   { confirmar:'CONFIRMADO', cancelar:'CANCELADO', finalizar:'FINALIZADO' }
};
const gestionaEstados = () => ROL==='ADMIN' || ROL==='RECEPCIONISTA';
let patronEstadoOK = { alquileres:0, catering:0 };   // 0 = PATCH /{id}/{accion} (convención del proyecto)
const patronesEstado = base => [
    (id,acc)    =>({ ruta:base+'/'+id+'/'+acc,        metodo:'PATCH', cuerpo:null }),   // ← el que usa el backend
    (id,acc,est)=>({ ruta:base+'/'+id+'/estado/'+est, metodo:'PATCH', cuerpo:null }),
    (id,acc,est)=>({ ruta:base+'/'+id+'/estado',      metodo:'PATCH', cuerpo:{estado:est} }),
    (id,acc,est)=>({ ruta:base+'/'+id,                metodo:'PATCH', cuerpo:{estado:est} }),
    (id,acc)    =>({ ruta:base+'/'+id+'/'+acc,        metodo:'PUT',   cuerpo:null }),
    (id,acc)    =>({ ruta:base+'/'+id+'/'+acc,        metodo:'POST',  cuerpo:null })
];
async function apiCambiarEstado(clave, id, accion, motivo){
    const base = clave==='catering' ? await rutaCatering() : RUTAS.alquileres;
    const sufijo = (motivo && accion==='cancelar') ? '?motivo='+encodeURIComponent(motivo) : '';
    const patrones = patronesEstado(base);
    const orden = patronEstadoOK[clave]!=null ? [patronEstadoOK[clave]] : patrones.map((_,i)=>i);
    let ultimo = null;
    for (const i of orden){
        const p = patrones[i](id, accion, ACCION_ESTADO[clave][accion]);
        try{
            const r = await api(p.ruta+sufijo, p.metodo, p.cuerpo);
            patronEstadoOK[clave] = i;        // este patrón funcionó: lo reutilizamos en adelante
            return r;
        }catch(e){
            if (e.status!==404 && e.status!==405) throw e;   // 400/401/403 = error real → se muestra
            ultimo = e;                                      // 404/405 = ese endpoint no existe → siguiente
        }
    }
    throw { message:'Tu backend no expone un endpoint de cambio de estado para '
            +(clave==='catering'?'el catering':'la reserva')
            +'. Revisa los @PatchMapping del Controller (guía en backend-gestion-estados.md).'};
}
function botonEstado(fn, id, accion, txt, cls){
    return '<button class="btn-estado '+cls+'" onclick="'+fn+'('+id+',\''+accion+'\')">'+txt+'</button>';
}
function botonesEstadoAlquiler(a){
    if (!gestionaEstados()) return '<span style="color:#8a93ab">—</span>';
    const est = (a.estado||'').toUpperCase();
    if (est==='PENDIENTE')  return botonEstado('cambiarEstadoAlquiler',a.id,'confirmar','✔ Confirmar','ok')
        +' '+botonEstado('cambiarEstadoAlquiler',a.id,'cancelar','✖ Rechazar','no');
    if (est==='CONFIRMADA') return botonEstado('cambiarEstadoAlquiler',a.id,'finalizar','🏁 Finalizar','fin')
        +' '+botonEstado('cambiarEstadoAlquiler',a.id,'cancelar','✖ Cancelar','no');
    return '<span style="color:#8a93ab">—</span>';
}
async function cambiarEstadoAlquiler(id, accion){
    try{
        const motivo = accion==='cancelar' ? ((prompt('Motivo de la cancelación (opcional):')||'').trim()) : null;
        const a = await apiCambiarEstado('alquileres', id, accion, motivo);
        banner('✅ Reserva '+((a&&a.codigo)||('#'+id))+' actualizada a '+((a&&a.estado)||ACCION_ESTADO.alquileres[accion])+'.','ok');
        cargarAlquileres();
        if (puede('agenda') && !g('agenda').classList.contains('hidden')) cargarAgenda();
    }catch(e){ banner('❌ '+e.message,'err'); }
}
function botonesEstadoCatering(c){
    if (!gestionaEstados()) return '<span style="color:#8a93ab">—</span>';
    const est = (c.estado||'').toUpperCase();
    if (est==='PENDIENTE')  return botonEstado('cambiarEstadoCatering',c.id,'confirmar','✔ Confirmar','ok')
        +' '+botonEstado('cambiarEstadoCatering',c.id,'cancelar','✖ Rechazar','no');
    if (est==='CONFIRMADO') return botonEstado('cambiarEstadoCatering',c.id,'finalizar','🏁 Finalizar','fin')
        +' '+botonEstado('cambiarEstadoCatering',c.id,'cancelar','✖ Cancelar','no');
    return '<span style="color:#8a93ab">—</span>';
}
async function cambiarEstadoCatering(id, accion){
    try{
        const motivo = accion==='cancelar' ? ((prompt('Motivo de la cancelación (opcional):')||'').trim()) : null;
        const c = await apiCambiarEstado('catering', id, accion, motivo);
        banner('✅ Catering '+((c&&c.codigo)||('#'+id))+' actualizado a '+((c&&c.estado)||ACCION_ESTADO.catering[accion])+'.','ok');
        cargarCatering();
        if (puede('agenda') && !g('agenda').classList.contains('hidden')) cargarAgenda();
    }catch(e){ banner('❌ '+e.message,'err'); }
}

async function cargarAlquileres(){
    try{
        g('alquileresTabla').innerHTML = '<table><tr><td colspan="8" class="cargando">Cargando</td></tr></table>';
        const lista = await cargarTodo(RUTAS.alquileres);
        g('alquileresTabla').innerHTML = lista.length
            ? '<table><thead><tr><th>Código</th><th>Espacio</th><th>Cliente</th><th>Fecha</th><th>Horario</th><th>Estado</th><th>Costo</th><th>Acciones</th></tr></thead><tbody>'
            + lista.map(a=>'<tr><td><b>'+(a.codigo||a.id)+'</b></td><td>'+nomEspacio(a)+'</td><td>'+nomCliente(a)
                +'</td><td>'+(a.fecha||'—')+'</td><td>'+(a.horaInicio||'?')+'–'+(a.horaFin||'?')
                +'</td><td>'+(a.estado||'—')+'</td><td>'+(a.costoTotal!=null?'$'+a.costoTotal
                    :(a.costo!=null?'$'+a.costo:(a.total!=null?'$'+a.total:'—')))+'</td>'
                +'<td>'+botonesEstadoAlquiler(a)+'</td></tr>').join('')+'</tbody></table>'
            : '<p style="color:#6a748c">Aún no hay reservas de espacios.</p>';
        filtrarTabla('filtroAlquileres','alquileresTabla');
    }catch(e){ g('alquileresTabla').innerHTML = '<p style="color:var(--err)">'+e.message+'</p>'; }
}

// ───────── CATERING ─────────
async function cargarCatering(){
    try{
        g('cateringTabla').innerHTML = '<table><tr><td colspan="8" class="cargando">Cargando</td></tr></table>';
        const lista = await cargarTodo(await rutaCatering());
        g('cateringTabla').innerHTML = lista.length
            ? '<table><thead><tr><th>Código</th><th>Cliente</th><th>Fecha</th><th>Asistentes</th><th>Precio p/p</th><th>Total</th><th>Estado</th><th>Acciones</th></tr></thead><tbody>'
            + lista.map(c=>{
                const pp  = c.precioPersona ?? c.precioPorPersona ?? c.precio;
                const tot = c.total ?? c.costoTotal ?? c.montoTotal;
                return '<tr><td><b>'+(c.codigo||c.id)+'</b></td><td>'+nomCliente(c)+'</td><td>'+(c.fechaEvento||c.fecha||'—')
                    +'</td><td>'+(c.asistentes ?? c.numeroAsistentes ?? '—')+'</td><td>'+(pp!=null?'$'+pp:'—')
                    +'</td><td><b>'+(tot!=null?'$'+tot:'—')+'</b></td><td>'+(c.estado||'—')+'</td>'
                    +'<td>'+botonesEstadoCatering(c)+'</td></tr>';
            }).join('')+'</tbody></table>'
            : '<p style="color:#6a748c">Aún no hay servicios de catering.</p>';
        filtrarTabla('filtroCatering','cateringTabla');
    }catch(e){ g('cateringTabla').innerHTML = '<p style="color:var(--err)">'+e.message+'</p>'; }
}

async function crearCatering(){
    const v = id => g(id).value;
    const faltan = [];
    if (!v('catCliente'))    faltan.push('cliente (escribe y elige una sugerencia)');
    if (!v('catFecha'))      faltan.push('fecha del evento');
    if (!v('catAsistentes')) faltan.push('número de asistentes');
    if (!v('catPrecio'))     faltan.push('precio por persona');
    if (faltan.length){ banner('❌ Falta completar: '+faltan.join(' · ')+'.','err'); return; }
    try{
        const c = await api(RUTA_CATERING_FIJA,'POST',{
            clienteId:+v('catCliente'), cotizacionId:g('cotizacionId')?(v('cotizacionId')||null):null,
            tipoServicio:v('catTipo'),
            fechaEvento:v('catFecha'), fecha:v('catFecha'),
            horaEntrega:v('catHora')||null,
            lugar:(v('catLugar')==='__otro' ? v('catLugarOtro').trim() : v('catLugar'))||null,
            numeroAsistentes:+v('catAsistentes'), asistentes:+v('catAsistentes'),
            precioPorPersona:+v('catPrecio'), precioPersona:+v('catPrecio'), precio:+v('catPrecio'),
            menu:v('catMenu').trim()||null, descripcion:v('catMenu').trim()||null,
            observaciones:v('catObs').trim()||null
        });
        const tot = c.total ?? c.costoTotal ?? c.montoTotal;
        banner('✅ Catering contratado'+(tot!=null?' — Total calculado por el backend: $'+tot:'')+'.','ok');
        ['catFecha','catAsistentes','catPrecio','catHora','catLugar','catLugarOtro','catMenu','catObs'].forEach(id=>g(id).value='');
        cargarCatering();
    }catch(e){ banner('❌ '+e.message,'err'); }
}

// ───────── PAGOS ─────────
async function cargarOpcionesPago(){
    try{
        const ins = await cargarTodo('/inscripciones');
        actualizarBuscador('pagIns', ins);
    }catch(e){ actualizarBuscador('pagIns', []); }

    cargarPagosDeInscripcion();
}
async function registrarPago(){
    if (!g('pagIns').value || !g('pagMonto').value) {
        banner('❌ Selecciona una inscripción y escribe el monto.','err'); return;
    }
    try{
        const p = await api('/pagos','POST',
            { inscripcionId:+g('pagIns').value, monto:+g('pagMonto').value, metodoPago:g('pagMetodo').value });
        banner('✅ Pago '+p.codigo+' registrado por $'+p.monto+' ('+p.metodoPago+')','ok');
        cargarPagosDeInscripcion();
        verSaldo();
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

    cargarPagosDeInscripcion();
}

// ───────── PAGOS: listado + validación de caja (cambio de estado) ─────────
async function cargarPagosDeInscripcion(){
    if (!g('pagIns').value) { g('pagosTabla').innerHTML=''; return; }
    try{
        const lista = await cargarTodo('/pagos', 'inscripcionId='+g('pagIns').value);
        g('pagosTabla').innerHTML = lista.length
            ? '<table><thead><tr><th>Recibo</th><th>Monto</th><th>Método</th><th>Estado</th><th>Acción</th></tr></thead><tbody>'
            + lista.map(p=>{
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
        paginarVista('pagosTabla');
    }catch(e){ g('pagosTabla').innerHTML = '<p style="color:var(--err)">'+e.message+'</p>'; }
}
async function cambiarEstadoPago(id, nuevoEstado){
    try{
        const p = await api('/pagos/'+id+'/estado/'+nuevoEstado,'PATCH');
        banner('✅ Pago '+p.codigo+' actualizado a '+p.estado,'ok');
        cargarPagosDeInscripcion();
        verSaldo();
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
        const lista = await cargarTodo('/usuarios');
        g('usuariosTabla').innerHTML = '<table><thead><tr><th>Nombre</th><th>Correo</th><th>Rol</th><th>Estado</th><th>Cambio pendiente</th></tr></thead><tbody>'
            + lista.map(u=>'<tr><td>'+u.nombre+'</td><td>'+u.correo+'</td><td><b>'+u.rol+'</b></td><td>'
                +(u.activo?'✅':'⛔')+'</td><td>'+(u.debeCambiarPassword?'🔑 Sí':'—')+'</td></tr>').join('')+'</tbody></table>';
        paginarVista('usuariosTabla');
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

// ───────── INIT: buscadores con filtro (sustituyen a los selects largos) ─────────
const ETIQ_CLIENTE = c => (c.id)+' — '+(c.nombre||'')+' ('+(c.tipoCliente||'')+')';
crearBuscador('insCliente','🔎 Cliente: escribe id, nombre o tipo para buscar…', ETIQ_CLIENTE);
crearBuscador('alqCliente','🔎 Cliente: escribe id, nombre o tipo para buscar…', ETIQ_CLIENTE);
crearBuscador('catCliente','🔎 Cliente: escribe id, nombre o tipo para buscar…', ETIQ_CLIENTE);
crearBuscador('insCurso','🔎 Curso: escribe código o nombre para buscar…',
    c=>(c.codigo||c.id)+' — '+(c.nombre||'')+' (cupo '+(c.cupoMaximo??'?')+')');
crearBuscador('alqEspacio','🔎 Espacio: escribe código, nombre o tipo para buscar…',
    e=>(e.codigo||e.id)+' — '+(e.nombre||'')+' ['+(e.tipo||'?')+'] (cap. '+(e.capacidad??'?')+')');
crearBuscador('pagIns','🔎 Inscripción: escribe código o cliente para buscar…',
    i=>(i.codigo||i.id)+' — '+(i.clienteNombre||'')+' ('+(i.estado||'')+')', ()=>verSaldo());

// ───────── AUTOLOGIN si hay token guardado ─────────
if (TOKEN) {
    iniciarApp();
    if (localStorage.getItem('cfcDebeCambiar') === '1') abrirCambioPass(true);
}

// ───────── Salidas extra del modal: ✕, Escape y clic fuera de la tarjeta ─────────
g('btnXPassword')?.addEventListener('click', cerrarCambioPass);
document.addEventListener('keydown', e => {
    if (e.key === 'Escape' && !g('modalPass').classList.contains('hidden')) cerrarCambioPass();
});
g('modalPass').addEventListener('click', e => {
    if (e.target === g('modalPass')) cerrarCambioPass();
});
