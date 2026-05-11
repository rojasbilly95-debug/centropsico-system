let currentUser = null;

function validateSession() {
    const storedUser = localStorage.getItem("currentUser");
    const token = localStorage.getItem("token");

    if (!storedUser || !token) {
        localStorage.clear();
        window.location.href = "/login.html";
        return false;
    }

    currentUser = JSON.parse(storedUser);
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

function logout() {
    localStorage.removeItem("currentUser");
    localStorage.removeItem("token");
    window.location.replace("/login.html");
}