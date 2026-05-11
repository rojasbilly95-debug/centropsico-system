window.addEventListener("DOMContentLoaded", async () => {
    const user = localStorage.getItem("currentUser");
    const token = localStorage.getItem("token");

    if (!user || !token) return;

    try {
        const response = await fetch("/api/auth/validate", {
            method: "GET",
            headers: {
                "Authorization": "Bearer " + token
            }
        });

        if (!response.ok) {
            localStorage.clear();
            return;
        }

        const data = await response.json();

        if (data.valid) {
            window.location.replace("/index.html");
        } else {
            localStorage.clear();
        }

    } catch (error) {
        // Si backend no responde (ej: reinicio)
        localStorage.clear();
    }
});

async function login() {
    const email = document.getElementById("loginEmail").value.trim();
    const password = document.getElementById("loginPassword").value.trim();
    const message = document.getElementById("loginMessage");

    if (!email || !password) {
        message.textContent = "Ingresa correo y contraseña";
        message.className = "login-message error";
        return;
    }

    try {
        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) {
            throw new Error("Correo o contraseña incorrectos");
        }

        const data = await response.json();

        // Guardar token
        localStorage.setItem("token", data.token);

        // Guardar usuario
        localStorage.setItem("currentUser", JSON.stringify({
            id: data.id,
            firstName: data.firstName,
            lastName: data.lastName,
            email: data.email,
            role: data.role
        }));

        window.location.replace("/index.html");

    } catch (error) {
        message.textContent = error.message;
        message.className = "login-message error";
    }
}