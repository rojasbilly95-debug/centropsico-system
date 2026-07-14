let currentUser = null;

function validateSession() {
    const storedUser = localStorage.getItem("currentUser");
    const token = localStorage.getItem("token");

    if (!storedUser || !token) {
        localStorage.clear();
        window.location.href = "/login.html";
        return false;
    }

    try {
        currentUser = JSON.parse(storedUser);
    } catch (error) {
        localStorage.clear();
        window.location.href = "/login.html";
        return false;
    }

    if (!currentUser || !currentUser.email || !currentUser.role) {
        localStorage.clear();
        window.location.href = "/login.html";
        return false;
    }

    return true;
}

function getAuthHeaders() {
    const token = localStorage.getItem("token");

    return {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + token
    };
}

async function authFetch(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            ...getAuthHeaders(),
            ...(options.headers || {})
        }
    });

    if (response.status === 401) {
        localStorage.clear();
        window.location.href = "/login.html";
        return null;
    }

    if (response.status === 403) {
        console.warn("Acceso denegado:", url);
        return response;
    }

    return response;
}

async function logout() {
    try {
        const token = localStorage.getItem("token");

        if (token) {
            await fetch("/api/auth/logout", {
                method: "POST",
                headers: {
                    "Authorization": "Bearer " + token
                }
            });
        }

    } catch (error) {
        console.warn("No se pudo registrar cierre de sesión:", error);
    } finally {
        localStorage.removeItem("currentUser");
        localStorage.removeItem("token");
        window.location.replace("/login.html");
    }
}