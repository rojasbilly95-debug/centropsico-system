let editingUserId = null;
let editingUserActive = true;

let usersData = [];
let currentUserPage = 1;
const usersPerPage = 10;

function getUserFormData() {
    return {
        firstName: document.getElementById("userFirstName").value.trim(),
        lastName: document.getElementById("userLastName").value.trim(),
        email: document.getElementById("userEmail").value.trim(),
        password: document.getElementById("userPassword").value,
        role: document.getElementById("userRole").value,
        active: editingUserActive
    };
}

function validateUserForm(data) {
    if (!data.firstName) return "Ingrese los nombres del usuario";
    if (!data.lastName) return "Ingrese los apellidos del usuario";
    if (!data.email) return "Ingrese el correo del usuario";
    if (!editingUserId && !data.password) return "Ingrese la contraseña del usuario";
    if (!data.role) return "Seleccione un rol";
    return null;
}

async function saveUser() {
    if (editingUserId) {
        await updateUser();
    } else {
        await createUser();
    }
}

async function createUser() {
    const data = getUserFormData();
    const validationError = validateUserForm(data);

    if (validationError) {
        showUserMessage(validationError, "error");
        return;
    }

    try {
        const response = await authFetch(`${baseUrl}/users`, {
            method: "POST",
            body: JSON.stringify(data)
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            showUserMessage(result.message || "Error al guardar usuario", "error");
            return;
        }

        showUserMessage("Usuario guardado correctamente", "success");

        clearUserForm();
        await loadUsers();

    } catch (error) {
        showUserMessage("Error de conexión con el servidor", "error");
    }
}

async function updateUser() {
    const data = getUserFormData();
    const validationError = validateUserForm(data);

    if (validationError) {
        showUserMessage(validationError, "error");
        return;
    }

    try {
        const response = await authFetch(`${baseUrl}/users/${editingUserId}`, {
            method: "PUT",
            body: JSON.stringify(data)
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            showUserMessage(result.message || "Error al actualizar usuario", "error");
            return;
        }

        showUserMessage("Usuario actualizado correctamente", "success");

        cancelUserEdit();
        await loadUsers();

    } catch (error) {
        showUserMessage("Error de conexión con el servidor", "error");
    }
}

async function loadUsers() {
    try {
        const response = await authFetch(`${baseUrl}/users`);
        if (!response) return;

        usersData = await response.json();
        currentUserPage = 1;

        renderUserTable(getFilteredUsers());

    } catch (error) {
        showUserMessage("Error al listar usuarios", "error");
    }
}

function renderUserTable(data) {
    const tbody = document.getElementById("userTableBody");
    tbody.innerHTML = "";

    const totalPages = Math.ceil(data.length / usersPerPage) || 1;

    if (currentUserPage > totalPages) currentUserPage = totalPages;

    const start = (currentUserPage - 1) * usersPerPage;
    const end = start + usersPerPage;
    const pageData = data.slice(start, end);

    if (pageData.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align:center;">
                    No se encontraron usuarios
                </td>
            </tr>
        `;
        return;
    }

    pageData.forEach(user => {
        tbody.innerHTML += `
            <tr>
                <td>${user.id ?? ""}</td>
                <td>${user.firstName ?? ""}</td>
                <td>${user.lastName ?? ""}</td>
                <td>${user.email ?? ""}</td>
                <td><span class="badge badge-role">${user.role ?? ""}</span></td>
                <td>
                    <span class="status-pill ${user.active ? "active" : "inactive"}">
                        ${user.active ? "Activo" : "Inactivo"}
                    </span>
                </td>
                <td>
                    <button class="btn-secondary" onclick="startEditUser(${user.id})">
                        Editar
                    </button>

                    <button class="${user.active ? "btn-danger-soft" : "btn-secondary"}"
                            onclick="toggleUserStatus(${user.id}, ${user.active})">
                        ${user.active ? "Desactivar" : "Activar"}
                    </button>
                </td>
            </tr>
        `;
    });

    const pageInfo = document.getElementById("userPageInfo");
    if (pageInfo) {
        pageInfo.textContent = `Página ${currentUserPage} de ${totalPages}`;
    }
}

function getFilteredUsers() {
    const input = document.querySelector("#userListModal .table-search");
    const search = input ? input.value.toLowerCase() : "";

    if (!search) return usersData;

    return usersData.filter(user => {
        const text = `
            ${user.id ?? ""}
            ${user.firstName ?? ""}
            ${user.lastName ?? ""}
            ${user.email ?? ""}
            ${user.role ?? ""}
            ${user.active ? "activo" : "inactivo"}
        `.toLowerCase();

        return text.includes(search);
    });
}

function filterUserTable() {
    currentUserPage = 1;
    renderUserTable(getFilteredUsers());
}

function changeUserPage(direction) {
    const filtered = getFilteredUsers();
    const totalPages = Math.ceil(filtered.length / usersPerPage) || 1;

    currentUserPage += direction;

    if (currentUserPage < 1) currentUserPage = 1;
    if (currentUserPage > totalPages) currentUserPage = totalPages;

    renderUserTable(filtered);
}

async function toggleUserList() {
    document.getElementById("userListModal").classList.remove("hidden");
    await loadUsers();
}

function closeUserList() {
    document.getElementById("userListModal").classList.add("hidden");
}

function startEditUser(id) {
    const user = usersData.find(u => u.id === id);

    if (!user) {
        showUserMessage("No se encontró el usuario seleccionado", "error");
        return;
    }

    editingUserId = id;
    editingUserActive = user.active;

    document.getElementById("userFirstName").value = user.firstName ?? "";
    document.getElementById("userLastName").value = user.lastName ?? "";
    document.getElementById("userEmail").value = user.email ?? "";
    document.getElementById("userPassword").value = "";
    document.getElementById("userRole").value = user.role ?? "ADMIN";

    document.getElementById("userSaveButton").textContent = "Actualizar usuario";
    document.getElementById("userCancelEditButton").style.display = "inline-block";

    closeUserList();

    document.getElementById("users").scrollIntoView({
        behavior: "smooth",
        block: "start"
    });

    showUserMessage(`Editando usuario: ${user.firstName} ${user.lastName}`, "info");
}

function cancelUserEdit() {
    editingUserId = null;
    editingUserActive = true;
    clearUserForm();

    document.getElementById("userSaveButton").textContent = "Guardar usuario";
    document.getElementById("userCancelEditButton").style.display = "none";

    showUserMessage("Edición cancelada", "info");
}

async function toggleUserStatus(id, isActive) {
    const actionText = isActive ? "desactivar" : "activar";
    const confirmText = isActive ? "Sí, desactivar" : "Sí, activar";

    const result = await Swal.fire({
        title: `¿Deseas ${actionText} este usuario?`,
        text: isActive
            ? "El usuario no será eliminado, solo quedará inactivo."
            : "El usuario volverá a tener acceso al sistema.",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: confirmText,
        cancelButtonText: "Cancelar",
        confirmButtonColor: "#0f3d66"
    });

    if (!result.isConfirmed) return;

    try {
        const response = await authFetch(`${baseUrl}/users/${id}/toggle-status`, {
            method: "PATCH"
        });

        if (!response) return;

        const updatedUser = await response.json();

        if (!response.ok) {
            showUserMessage(updatedUser.message || "No se pudo cambiar el estado del usuario", "error");
            return;
        }

        await Swal.fire({
            icon: "success",
            title: "Estado actualizado",
            text: `Usuario ${updatedUser.active ? "activado" : "desactivado"} correctamente`,
            timer: 1500,
            showConfirmButton: false
        });

        await loadUsers();

    } catch (error) {
        showUserMessage("Error de conexión con el servidor", "error");
    }
}

function clearUserForm() {
    document.getElementById("userFirstName").value = "";
    document.getElementById("userLastName").value = "";
    document.getElementById("userEmail").value = "";
    document.getElementById("userPassword").value = "";
    document.getElementById("userRole").value = "ADMIN";
}

function showUserMessage(message, type = "info") {
    const box = document.getElementById("userResult");
    if (!box) return;

    box.textContent = message;

    box.classList.remove("message-success", "message-error", "message-info");

    if (type === "success") {
        box.classList.add("message-success");
    } else if (type === "error") {
        box.classList.add("message-error");
    } else {
        box.classList.add("message-info");
    }
}