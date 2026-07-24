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
    const tbody =
        document.getElementById("userTableBody");

    if (!tbody) {
        return;
    }

    tbody.innerHTML = "";

    const totalPages =
        Math.ceil(data.length / usersPerPage) || 1;

    if (currentUserPage > totalPages) {
        currentUserPage = totalPages;
    }

    const start =
        (currentUserPage - 1) * usersPerPage;

    const end =
        start + usersPerPage;

    const pageData =
        data.slice(start, end);

    if (pageData.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td
                    colspan="8"
                    style="text-align:center;"
                >
                    No se encontraron usuarios
                </td>
            </tr>
        `;

        const pageInfo =
            document.getElementById(
                "userPageInfo"
            );

        if (pageInfo) {
            pageInfo.textContent =
                "Página 1 de 1";
        }

        return;
    }

    pageData.forEach(user => {
        tbody.innerHTML += `
            <tr>
                <td>
                    ${user.id ?? ""}
                </td>

                <td class="profile-photo-cell">
                    ${buildUserTableAvatar(user)}
                </td>

                <td>
                    ${escapeUserTableHtml(
                        user.firstName ?? ""
                    )}
                </td>

                <td>
                    ${escapeUserTableHtml(
                        user.lastName ?? ""
                    )}
                </td>

                <td>
                    ${escapeUserTableHtml(
                        user.email ?? ""
                    )}
                </td>

                <td>
                    <span class="badge badge-role">
                        ${escapeUserTableHtml(
                            user.role ?? ""
                        )}
                    </span>
                </td>

                <td>
                    <span class="status-pill ${
                        user.active
                            ? "active"
                            : "inactive"
                    }">
                        ${
                            user.active
                                ? "Activo"
                                : "Inactivo"
                        }
                    </span>
                </td>

                <td>
                    <button
                        type="button"
                        class="btn-secondary"
                        onclick="startEditUser(${user.id})"
                    >
                        Editar
                    </button>

                    <button
                        type="button"
                        class="${
                            user.active
                                ? "btn-danger-soft"
                                : "btn-secondary"
                        }"
                        onclick="toggleUserStatus(
                            ${user.id},
                            ${Boolean(user.active)}
                        )"
                    >
                        ${
                            user.active
                                ? "Desactivar"
                                : "Activar"
                        }
                    </button>

                    ${
                        user.active
                            ? `
                                <button
                                    type="button"
                                    class="table-action-btn info"
                                    onclick="openUserNotificationModal(
                                        ${user.id}
                                    )"
                                >
                                    Notificar
                                </button>
                            `
                            : ""
                    }
                </td>
            </tr>
        `;
    });

    const pageInfo =
        document.getElementById(
            "userPageInfo"
        );

    if (pageInfo) {
        pageInfo.textContent =
            `Página ${currentUserPage} de ${totalPages}`;
    }
}

function escapeUserTableHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function buildUserTableAvatar(user) {
    const imageSource =
        getUserTableImageSource(user);

    const initials =
        getUserTableInitials(user);

    if (!imageSource) {
        return `
            <div
                class="table-profile-avatar initials"
                title="Usuario sin foto"
            >
                ${escapeUserTableHtml(initials)}
            </div>
        `;
    }

    return `
        <button
            type="button"
            class="table-profile-photo-button"
            onclick="openUserProfilePhoto(${Number(user.id)})"
            title="Ver fotografía"
        >
            <img
                src="${escapeUserTableHtml(imageSource)}"
                alt="Foto de ${escapeUserTableHtml(
                    getUserTableFullName(user)
                )}"
                class="table-profile-avatar"
            >
        </button>
    `;
}

function getUserTableImageSource(user) {
    const base64Image =
        String(
            user?.profileImageBase64 || ""
        ).trim();

    if (
        /^data:image\/(jpeg|jpg|png|webp);base64,/i
            .test(base64Image)
    ) {
        return base64Image;
    }

    const imageUrl =
        String(
            user?.profileImageUrl || ""
        ).trim();

    if (
        imageUrl.startsWith("/") ||
        imageUrl.startsWith("https://") ||
        imageUrl.startsWith("http://")
    ) {
        return imageUrl;
    }

    return "";
}

function getUserTableInitials(user) {
    const firstInitial =
        String(
            user?.firstName || ""
        )
            .trim()
            .charAt(0);

    const lastInitial =
        String(
            user?.lastName || ""
        )
            .trim()
            .charAt(0);

    return (
        `${firstInitial}${lastInitial}`
            .toUpperCase() || "US"
    );
}

function getUserTableFullName(user) {
    const fullName = `
        ${user?.firstName || ""}
        ${user?.lastName || ""}
    `
        .replace(/\s+/g, " ")
        .trim();

    return fullName || "Usuario";
}

function openUserProfilePhoto(userId) {
    const user =
        usersData.find(
            item =>
                Number(item.id) ===
                Number(userId)
        );

    if (!user) {
        Swal.fire(
            "Error",
            "No se encontró el usuario.",
            "error"
        );

        return;
    }

    const imageSource =
        getUserTableImageSource(user);

        if (!imageSource) {
            return;
        }

    Swal.fire({
        title: getUserTableFullName(user),
        imageUrl: imageSource,
        imageAlt: `Foto de ${getUserTableFullName(user)}`,
        showCloseButton: true,
        showConfirmButton: false,
        width: 600,
        customClass: {
            image: "profile-photo-expanded"
        }
    });
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

async function openUserNotificationModal(userId) {
    if (!userId) return;

    const user = typeof usersData !== "undefined"
        ? usersData.find(item => Number(item.id) === Number(userId))
        : null;

    const userName = getUserNotificationDisplayName(user);
    const userRole = user?.role || "SIN_ROL";
    const userEmail = user?.email || "";

    const result = await Swal.fire({
        title: "Enviar notificación",
        html: `
            <div class="admin-notify-modal">

                <div class="admin-notify-user-card">
                    <strong>${escapeUserNotificationHtml(userName)}</strong>
                    <span>${escapeUserNotificationHtml(formatUserNotificationRole(userRole))}</span>
                    <small>${escapeUserNotificationHtml(userEmail)}</small>
                </div>

                <label>Asunto</label>
                <input 
                    id="adminNotifyTitle" 
                    class="swal2-input admin-notify-input" 
                    maxlength="120"
                    placeholder="Ejemplo: Recordatorio de citas">

                <label>Mensaje</label>
                <textarea 
                    id="adminNotifyMessage" 
                    class="admin-notify-textarea"
                    maxlength="700"
                    placeholder="Escribe el mensaje que recibirá el usuario en su campana."></textarea>
            </div>
        `,
        showCancelButton: true,
        confirmButtonText: "Enviar notificación",
        cancelButtonText: "Cancelar",
        confirmButtonColor: "#0f3d66",
        width: 620,
        focusConfirm: false,
        preConfirm: () => {
            const title = document.getElementById("adminNotifyTitle").value.trim();
            const message = document.getElementById("adminNotifyMessage").value.trim();

            if (!title) {
                Swal.showValidationMessage("Ingresa el asunto de la notificación.");
                return false;
            }

            if (title.length < 4) {
                Swal.showValidationMessage("El asunto debe tener al menos 4 caracteres.");
                return false;
            }

            if (!message) {
                Swal.showValidationMessage("Ingresa el mensaje de la notificación.");
                return false;
            }

            if (message.length < 8) {
                Swal.showValidationMessage("El mensaje debe tener al menos 8 caracteres.");
                return false;
            }

            return {
                title,
                message
            };
        }
    });

    if (!result.isConfirmed || !result.value) return;

    try {
        const response = await authFetch(`${baseUrl}/admin-notifications/users/${userId}`, {
            method: "POST",
            body: JSON.stringify(result.value)
        });

        if (!response) return;

        let data = {};

        try {
            data = await response.json();
        } catch (error) {
            data = {};
        }

        if (!response.ok) {
            Swal.fire(
                "Error",
                data.message || "No se pudo enviar la notificación.",
                "error"
            );
            return;
        }

        Swal.fire({
            icon: "success",
            title: "Notificación enviada",
            text: data.message || "El usuario recibirá el mensaje en su campana.",
            timer: 1800,
            showConfirmButton: false
        });

        if (typeof loadNotifications === "function") {
            await loadNotifications(false);
        }

        if (typeof loadAuditLogs === "function") {
            await loadAuditLogs();
        }

    } catch (error) {
        console.error("Error enviando notificación:", error);

        Swal.fire(
            "Error",
            "Error de conexión con el servidor.",
            "error"
        );
    }
}

function getUserNotificationDisplayName(user) {
    if (!user) return "Usuario seleccionado";

    const firstName = user.firstName || user.names || user.name || "";
    const lastName = user.lastName || user.surnames || "";

    const fullName = `${firstName} ${lastName}`.trim();

    return fullName || user.email || "Usuario seleccionado";
}

function formatUserNotificationRole(role) {
    const roles = {
        ADMIN: "Administrador",
        RECEPCIONISTA: "Recepción",
        PSICOLOGO: "Psicólogo"
    };

    return roles[role] || role || "Sin rol";
}

function escapeUserNotificationHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}