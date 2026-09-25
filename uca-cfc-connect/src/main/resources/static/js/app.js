// =========================================================
// CFC Connect - app.js
// Todo el comportamiento del front está en este único archivo.
// =========================================================

const API = "/api/v1";

// Usuarios de demostración (el backend todavía no tiene login real,
// ver SecurityConfig.java: por ahora /api/v1/** está abierto).
const USUARIOS_DEMO = {
    "admin@udb.edu.sv":   "Admin123!",
    "recep@udb.edu.sv":   "Recep123!",
    "contab@udb.edu.sv":  "Conta123!",
    "cliente@gmail.com":  "Cliente123!"
};

// ---------------------------------------------------------
// LOGIN
// ---------------------------------------------------------
function iniciarSesion() {
    const correo = document.getElementById("login-correo").value.trim();
    const clave = document.getElementById("login-clave").value;
    const error = document.getElementById("login-error");

    if (USUARIOS_DEMO[correo] && USUARIOS_DEMO[correo] === clave) {
        error.textContent = "";
        document.getElementById("usuario-actual").textContent = "Sesión: " + correo;
        document.getElementById("vista-login").classList.add("oculto");
        document.getElementById("vista-app").classList.remove("oculto");
        mostrarSeccion("agenda"); // sección inicial por defecto
    } else {
        error.textContent = "Correo o contraseña incorrectos.";
    }
}

function cerrarSesion() {
    document.getElementById("vista-app").classList.add("oculto");
    document.getElementById("vista-login").classList.remove("oculto");
    document.getElementById("login-correo").value = "";
    document.getElementById("login-clave").value = "";
}

// ---------------------------------------------------------
// MENÚ: mostrar solo una sección a la vez
// ---------------------------------------------------------
document.querySelectorAll(".menu-btn").forEach(boton => {
    boton.addEventListener("click", () => mostrarSeccion(boton.dataset.vista));
});

function mostrarSeccion(nombre) {
    document.querySelectorAll(".seccion").forEach(s => s.classList.remove("activa"));
    document.querySelectorAll(".menu-btn").forEach(b => b.classList.remove("activo"));

    document.getElementById("seccion-" + nombre).classList.add("activa");
    const btn = document.querySelector(`.menu-btn[data-vista="${nombre}"]`);
    if (btn) btn.classList.add("activo");
}

// ---------------------------------------------------------
// AGENDA (aún sin backend, solo placeholder)
// ---------------------------------------------------------
function consultarAgenda() {
    const fecha = document.getElementById("agenda-fecha").value;
    alert("Agenda para " + (fecha || "(sin fecha)") + ": módulo pendiente en el backend.");
}

// ---------------------------------------------------------
// CURSOS
// ---------------------------------------------------------
async function cargarCursos() {
    const tbody = document.querySelector("#tabla-cursos tbody");
    tbody.innerHTML = "<tr><td colspan='5'>Cargando...</td></tr>";
    try {
        const resp = await fetch(`${API}/cursos`);
        const cursos = await resp.json();
        tbody.innerHTML = "";
        (cursos.content || cursos).forEach(c => {
            tbody.innerHTML += `<tr>
        <td>${c.codigo ?? ""}</td>
        <td>${c.nombre ?? ""}</td>
        <td>${c.horas ?? ""}</td>
        <td>${c.precio ?? ""}</td>
        <td>${c.estado ?? ""}</td>
      </tr>`;
        });
    } catch (e) {
        tbody.innerHTML = "<tr><td colspan='5'>No se pudo cargar (¿backend encendido?)</td></tr>";
    }
}

// ---------------------------------------------------------
// CLIENTES
// ---------------------------------------------------------
async function guardarCliente() {
    const mensaje = document.getElementById("cli-mensaje");
    const cliente = {
        tipoCliente: document.getElementById("cli-tipo").value,
        nombre: document.getElementById("cli-nombre").value,
        dui: document.getElementById("cli-dui").value,
        nit: document.getElementById("cli-nit").value,
        correo: document.getElementById("cli-correo").value,
        telefono: document.getElementById("cli-telefono").value,
        contactoNombre: document.getElementById("cli-contacto").value,
        direccion: document.getElementById("cli-direccion").value
    };

    try {
        const resp = await fetch(`${API}/clientes`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(cliente)
        });
        if (!resp.ok) throw new Error(await resp.text());
        mensaje.style.color = "#1e7e34";
        mensaje.textContent = "Cliente guardado correctamente.";
        cargarClientes();
    } catch (e) {
        mensaje.style.color = "#c0392b";
        mensaje.textContent = "Error al guardar: revisa los datos.";
    }
}

async function cargarClientes() {
    const tbody = document.querySelector("#tabla-clientes tbody");
    tbody.innerHTML = "<tr><td colspan='4'>Cargando...</td></tr>";
    try {
        const resp = await fetch(`${API}/clientes`);
        const clientes = await resp.json();
        tbody.innerHTML = "";
        (clientes.content || clientes).forEach(c => {
            tbody.innerHTML += `<tr>
        <td>${c.id}</td>
        <td>${c.nombre ?? ""}</td>
        <td>${c.correo ?? ""}</td>
        <td>${c.telefono ?? ""}</td>
      </tr>`;
        });
    } catch (e) {
        tbody.innerHTML = "<tr><td colspan='4'>No se pudo cargar (¿backend encendido?)</td></tr>";
    }
}

// ---------------------------------------------------------
// INSCRIPCIONES
// ---------------------------------------------------------
async function inscribir() {
    const mensaje = document.getElementById("ins-mensaje");
    const body = {
        clienteId: Number(document.getElementById("ins-cliente").value),
        cursoId: Number(document.getElementById("ins-curso").value),
        observaciones: document.getElementById("ins-obs").value || null
    };

    try {
        const resp = await fetch(`${API}/inscripciones`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        });
        if (!resp.ok) throw new Error(await resp.text());
        const data = await resp.json();
        mensaje.style.color = "#1e7e34";
        mensaje.textContent = `Inscripción creada: ${data.codigo ?? data.id}`;
    } catch (e) {
        mensaje.style.color = "#c0392b";
        mensaje.textContent = "Error al inscribir: revisa los IDs.";
    }
}

// ---------------------------------------------------------
// PAGOS
// ---------------------------------------------------------
async function registrarPago() {
    const mensaje = document.getElementById("pago-mensaje");
    const body = {
        inscripcionId: Number(document.getElementById("pago-inscripcion").value),
        monto: Number(document.getElementById("pago-monto").value),
        metodoPago: document.getElementById("pago-metodo").value
    };

    try {
        const resp = await fetch(`${API}/pagos`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        });
        if (!resp.ok) throw new Error(await resp.text());
        mensaje.style.color = "#1e7e34";
        mensaje.textContent = "Pago registrado correctamente.";
    } catch (e) {
        mensaje.style.color = "#c0392b";
        mensaje.textContent = "Error al registrar el pago.";
    }
}

async function consultarSaldo() {
    const resultado = document.getElementById("saldo-resultado");
    const inscripcionId = document.getElementById("pago-inscripcion").value;

    if (!inscripcionId) {
        resultado.style.color = "#c0392b";
        resultado.textContent = "Escribe primero el ID de inscripción.";
        return;
    }

    try {
        const resp = await fetch(`${API}/pagos/resumen/inscripcion/${inscripcionId}`);
        if (!resp.ok) throw new Error(await resp.text());
        const r = await resp.json();
        resultado.style.color = "#1e7e34";
        resultado.textContent =
            `Precio curso: ${r.precioCurso} | Pagado: ${r.totalPagado} | Saldo pendiente: ${r.saldoPendiente}`;
    } catch (e) {
        resultado.style.color = "#c0392b";
        resultado.textContent = "No se pudo consultar el saldo.";
    }
}