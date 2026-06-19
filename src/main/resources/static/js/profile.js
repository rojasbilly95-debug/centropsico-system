async function openProfileModal() {
    try {
        const profile = await fetchMyProfile();

        if (!profile) {
            Swal.fire("Error", "No se pudo cargar tu perfil.", "error");
            return;
        }

        syncCurrentUser(profile);

        const imageUrl = getProfileImageUrl(profile.profileImageUrl);
        const initials = getUserInitials(profile);

        await Swal.fire({
            title: "Mi perfil",
            width: 760,
            html: `
                <div class="profile-modal">

                    <div class="profile-header-card">
                        <div class="profile-avatar-preview" id="profileAvatarPreview">
                            ${
                                imageUrl
                                    ? `<img src="${imageUrl}?t=${Date.now()}" alt="Foto de perfil">`
                                    : `<span>${initials}</span>`
                            }
                        </div>

                        <div class="profile-header-info">
                            <strong>${escapeProfileHtml(profile.firstName)} ${escapeProfileHtml(profile.lastName)}</strong>
                            <span>${escapeProfileHtml(profile.email)}</span>
                            <small>${formatProfileRole(profile.role)}</small>
                        </div>
                    </div>

                    <div class="profile-section">
                        <h4>Datos personales</h4>

                        <div class="profile-grid">
                            <input id="profileFirstName" type="text" placeholder="Nombres" value="${escapeProfileAttr(profile.firstName)}">
                            <input id="profileLastName" type="text" placeholder="Apellidos" value="${escapeProfileAttr(profile.lastName)}">
                            <input id="profilePhone" type="text" placeholder="Teléfono" value="${escapeProfileAttr(profile.phone)}">
                            <input id="profileEmail" type="email" value="${escapeProfileAttr(profile.email)}" disabled>
                        </div>
                    </div>

                    <div class="profile-section">
                        <h4>Foto de perfil</h4>

                        <div class="profile-photo-actions">
                            <input id="profilePhotoInput" type="file" accept="image/png,image/jpeg,image/webp">
                        </div>

                        <p class="profile-help">
                            Selecciona una imagen JPG, PNG o WEBP. La foto se subirá automáticamente. Tamaño máximo: 5MB.
                        </p>
                    </div>

                    <div id="profileModalResult" class="profile-result"></div>

                </div>
            `,
            showCancelButton: true,
            showDenyButton: true,
            confirmButtonText: "Guardar datos",
            denyButtonText: "Cambiar contraseña",
            cancelButtonText: "Cerrar",
            focusConfirm: false,
            didOpen: () => {
                const photoInput = document.getElementById("profilePhotoInput");

                if (photoInput) {
                    photoInput.addEventListener("change", async () => {
                        previewSelectedProfilePhoto();

                        showProfileModalMessage("Subiendo foto de perfil...", true);

                        await uploadProfilePhotoFromModal();
                    });
                }
            },
            preConfirm: async () => {
                const updated = await updateMyProfileFromModal();

                if (!updated) {
                    return false;
                }

                return updated;
            }
        }).then(async result => {
            if (result.isConfirmed && result.value) {
                await Swal.fire({
                    icon: "success",
                    title: "Perfil actualizado",
                    text: "Tus datos fueron actualizados correctamente.",
                    timer: 1600,
                    showConfirmButton: false
                });
            }

            if (result.isDenied) {
                await openChangePasswordModal();
            }
        });

    } catch (error) {
        console.error("Error al abrir perfil:", error);
        Swal.fire("Error", "No se pudo abrir el perfil.", "error");
    }
}

async function fetchMyProfile() {
    const response = await authFetch(`${baseUrl}/profile/me`);

    if (!response) return null;

    const result = await response.json();

    if (!response.ok) {
        return null;
    }

    return result;
}

async function updateMyProfileFromModal() {
    const firstName = document.getElementById("profileFirstName").value.trim();
    const lastName = document.getElementById("profileLastName").value.trim();
    const phone = document.getElementById("profilePhone").value.trim();

    if (!firstName) {
        Swal.showValidationMessage("Ingresa tus nombres.");
        return false;
    }

    if (!lastName) {
        Swal.showValidationMessage("Ingresa tus apellidos.");
        return false;
    }

    const response = await authFetch(`${baseUrl}/profile/me`, {
        method: "PUT",
        body: JSON.stringify({
            firstName,
            lastName,
            phone
        })
    });

    if (!response) return false;

    const result = await response.json();

    if (!response.ok) {
        Swal.showValidationMessage(result.message || "No se pudo actualizar el perfil.");
        return false;
    }

    syncCurrentUser(result);
    refreshSidebarUser();

    return result;
}

async function uploadProfilePhotoFromModal() {
    const input = document.getElementById("profilePhotoInput");

    if (!input) {
        showProfileModalMessage("No se encontró el campo para seleccionar la foto.", false);
        return;
    }

    if (!input.files || input.files.length === 0) {
        showProfileModalMessage("Primero selecciona una imagen.", false);
        return;
    }

    const file = input.files[0];

    const allowedTypes = ["image/jpeg", "image/png", "image/webp"];

    if (!allowedTypes.includes(file.type)) {
        showProfileModalMessage("Formato no permitido. Usa JPG, PNG o WEBP.", false);
        return;
    }

    if (file.size > 5 * 1024 * 1024) {
        showProfileModalMessage("La imagen no debe superar 5MB.", false);
        return;
    }

    try {
        const formData = new FormData();
        formData.append("photo", file);

        const token = localStorage.getItem("token");

        const response = await fetch(`${baseUrl}/profile/photo`, {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + token
            },
            body: formData
        });

        const responseText = await response.text();

        let result = {};

        try {
            result = responseText ? JSON.parse(responseText) : {};
        } catch (error) {
            console.error("Respuesta no JSON:", responseText);
        }

        if (response.status === 401) {
            localStorage.clear();
            window.location.href = "/login.html";
            return;
        }

        if (!response.ok) {
            showProfileModalMessage(
                result.message ||
                result.error ||
                responseText ||
                `No se pudo subir la foto. Código: ${response.status}`,
                false
            );
            return;
        }

        syncCurrentUser(result);
        refreshSidebarUser();
        updateProfileModalAvatar(result);

        showProfileModalMessage("Foto actualizada correctamente.", true);

        input.value = "";

    } catch (error) {
        console.error("Error al subir foto:", error);
        showProfileModalMessage("Error de conexión al subir la foto.", false);
    }
}

async function openChangePasswordModal() {
    await Swal.fire({
        title: "Cambiar contraseña",
        width: 520,
        html: `
            <div class="profile-password-modal">
                <input id="profileCurrentPassword" type="password" placeholder="Contraseña actual">
                <input id="profileNewPassword" type="password" placeholder="Nueva contraseña">
                <input id="profileConfirmPassword" type="password" placeholder="Confirmar nueva contraseña">

                <p>
                    La nueva contraseña debe tener al menos 6 caracteres.
                </p>
            </div>
        `,
        showCancelButton: true,
        confirmButtonText: "Actualizar contraseña",
        cancelButtonText: "Cancelar",
        focusConfirm: false,
        preConfirm: async () => {
            const currentPassword = document.getElementById("profileCurrentPassword").value;
            const newPassword = document.getElementById("profileNewPassword").value;
            const confirmPassword = document.getElementById("profileConfirmPassword").value;

            if (!currentPassword) {
                Swal.showValidationMessage("Ingresa tu contraseña actual.");
                return false;
            }

            if (!newPassword || newPassword.length < 6) {
                Swal.showValidationMessage("La nueva contraseña debe tener al menos 6 caracteres.");
                return false;
            }

            if (newPassword !== confirmPassword) {
                Swal.showValidationMessage("Las contraseñas no coinciden.");
                return false;
            }

            const response = await authFetch(`${baseUrl}/profile/change-password`, {
                method: "PUT",
                body: JSON.stringify({
                    currentPassword,
                    newPassword,
                    confirmPassword
                })
            });

            if (!response) return false;

            const result = await response.json();

            if (!response.ok) {
                Swal.showValidationMessage(result.message || "No se pudo cambiar la contraseña.");
                return false;
            }

            return result;
        }
    }).then(result => {
        if (result.isConfirmed) {
            Swal.fire({
                icon: "success",
                title: "Contraseña actualizada",
                text: "Tu contraseña fue cambiada correctamente.",
                timer: 1700,
                showConfirmButton: false
            });
        }
    });
}

function previewSelectedProfilePhoto() {
    const input = document.getElementById("profilePhotoInput");
    const preview = document.getElementById("profileAvatarPreview");

    if (!input || !preview || !input.files || input.files.length === 0) return;

    const file = input.files[0];
    const imageUrl = URL.createObjectURL(file);

    preview.innerHTML = `<img src="${imageUrl}" alt="Vista previa">`;
}

function updateProfileModalAvatar(profile) {
    const preview = document.getElementById("profileAvatarPreview");

    if (!preview) return;

    const imageUrl = getProfileImageUrl(profile.profileImageUrl);
    const initials = getUserInitials(profile);

    preview.innerHTML = imageUrl
        ? `<img src="${imageUrl}?t=${Date.now()}" alt="Foto de perfil">`
        : `<span>${initials}</span>`;
}

function refreshSidebarUser() {
    if (!currentUser) return;

    const avatar = document.getElementById("sidebarUserAvatar");
    const name = document.getElementById("sidebarUserName");
    const role = document.getElementById("sidebarUserRole");

    if (name) {
        name.textContent = `${currentUser.firstName || ""} ${currentUser.lastName || ""}`.trim() || "Usuario";
    }

    if (role) {
        role.textContent = formatProfileRole(currentUser.role);
    }

    if (avatar) {
        const imageUrl = getProfileImageUrl(currentUser.profileImageUrl);

        if (imageUrl) {
            avatar.innerHTML = `<img src="${imageUrl}?t=${Date.now()}" alt="Foto de perfil">`;
        } else {
            avatar.textContent = getUserInitials(currentUser);
        }
    }
}

function syncCurrentUser(profile) {
    currentUser = {
        ...currentUser,
        id: profile.id,
        firstName: profile.firstName,
        lastName: profile.lastName,
        email: profile.email,
        role: profile.role,
        phone: profile.phone || "",
        profileImageUrl: profile.profileImageUrl || ""
    };

    localStorage.setItem("currentUser", JSON.stringify(currentUser));
}

function showProfileModalMessage(message, success) {
    const resultBox = document.getElementById("profileModalResult");

    if (!resultBox) return;

    resultBox.className = success
        ? "profile-result success"
        : "profile-result error";

    resultBox.textContent = message;
}

function getProfileImageUrl(url) {
    if (!url || String(url).trim() === "") return "";

    if (String(url).startsWith("http://") || String(url).startsWith("https://")) {
        return url;
    }

    return url;
}

function getUserInitials(user) {
    const first = user?.firstName ? user.firstName.charAt(0) : "";
    const last = user?.lastName ? user.lastName.charAt(0) : "";

    const initials = `${first}${last}`.toUpperCase();

    return initials || "US";
}

function formatProfileRole(role) {
    switch (role) {
        case "ADMIN":
            return "ADMIN";
        case "RECEPCIONISTA":
            return "RECEPCIÓN";
        case "PSICOLOGO":
            return "PSICÓLOGO";
        default:
            return role || "USUARIO";
    }
}

function escapeProfileHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function escapeProfileAttr(value) {
    return escapeProfileHtml(value);
}

function initProfileSidebarClick() {
    const sidebarUser = document.getElementById("sidebarUserProfile")
        || document.querySelector(".sidebar-user");

    if (!sidebarUser) {
        console.warn("No se encontró el bloque del usuario en el sidebar.");
        return;
    }

    sidebarUser.onclick = async function (event) {
        event.preventDefault();
        event.stopPropagation();

        if (typeof openProfileModal !== "function") {
            console.error("openProfileModal no está disponible.");
            Swal.fire("Error", "No se encontró la función del perfil.", "error");
            return;
        }

        await openProfileModal();
    };

    sidebarUser.style.cursor = "pointer";
    sidebarUser.setAttribute("title", "Abrir mi perfil");
}