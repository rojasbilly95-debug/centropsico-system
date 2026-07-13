let promotionsData = [];
let editingPromotionId = null;

async function loadPromotions() {
    const tbody = document.getElementById("promotionTableBody");
    const resultBox = document.getElementById("promotionResult");

    if (!tbody) return;

    try {
        const response = await authFetch(`${baseUrl}/promotions`);

        if (!response) return;

        const data = await response.json();

        if (!response.ok) {
            if (resultBox) resultBox.textContent = data.message || "No se pudieron cargar promociones.";
            return;
        }

        promotionsData = Array.isArray(data) ? data : [];
        renderPromotionTable();

    } catch (error) {
        console.error("Error cargando promociones:", error);
        if (resultBox) resultBox.textContent = "Error al cargar promociones.";
    }
}

function renderPromotionTable() {
    const tbody = document.getElementById("promotionTableBody");

    if (!tbody) return;

    tbody.innerHTML = "";

    if (!promotionsData || promotionsData.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="empty-table-message">
                    No hay promociones registradas.
                </td>
            </tr>
        `;
        return;
    }

    promotionsData.forEach(promotion => {
        tbody.innerHTML += `
            <tr>
                <td>${promotion.id ?? ""}</td>
                <td>${escapePromotionHtml(promotion.title || "")}</td>
                <td>${escapePromotionHtml(promotion.description || "")}</td>
                <td>${promotion.discountPercent != null ? promotion.discountPercent + "%" : "Sin descuento"}</td>
                <td>${promotion.startDate ?? "Sin inicio"}</td>
                <td>${promotion.endDate ?? "Sin fin"}</td>
                <td>
                    <span class="status-pill ${promotion.active ? "active" : "inactive"}">
                        ${promotion.active ? "Activo" : "Inactivo"}
                    </span>
                </td>
                <td>
                    <button class="table-action-btn" onclick="editPromotion(${promotion.id})">
                        Editar
                    </button>

                    <button class="table-action-btn ${promotion.active ? "danger" : ""}" onclick="togglePromotion(${promotion.id})">
                        ${promotion.active ? "Desactivar" : "Activar"}
                    </button>
                </td>
            </tr>
        `;
    });
}

async function savePromotion() {
    const resultBox = document.getElementById("promotionResult");

    const data = {
        title: document.getElementById("promotionTitle").value.trim(),
        description: document.getElementById("promotionDescription").value.trim(),
        discountPercent: document.getElementById("promotionDiscount").value
            ? Number(document.getElementById("promotionDiscount").value)
            : null,
        startDate: document.getElementById("promotionStartDate").value || null,
        endDate: document.getElementById("promotionEndDate").value || null,
        active: true
    };

    if (!data.title) {
        if (resultBox) resultBox.textContent = "Ingresa el título de la promoción.";
        return;
    }

    if (!data.description) {
        if (resultBox) resultBox.textContent = "Ingresa la descripción de la promoción.";
        return;
    }

    try {
        const url = editingPromotionId
            ? `${baseUrl}/promotions/${editingPromotionId}`
            : `${baseUrl}/promotions`;

        const method = editingPromotionId ? "PUT" : "POST";

        const response = await authFetch(url, {
            method,
            body: JSON.stringify(data)
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            if (resultBox) resultBox.textContent = result.message || "No se pudo guardar la promoción.";
            return;
        }

        if (resultBox) {
            resultBox.textContent = editingPromotionId
                ? "Promoción actualizada correctamente."
                : "Promoción registrada correctamente.";
        }

        clearPromotionForm();
        await loadPromotions();

        Swal.fire({
            icon: "success",
            title: "Promoción guardada",
            text: "La promoción ya estará disponible según su estado y vigencia.",
            timer: 1700,
            showConfirmButton: false
        });

    } catch (error) {
        console.error("Error guardando promoción:", error);
        if (resultBox) resultBox.textContent = "Error de conexión al guardar promoción.";
    }
}

function editPromotion(id) {
    const promotion = promotionsData.find(item => Number(item.id) === Number(id));

    if (!promotion) return;

    editingPromotionId = promotion.id;

    document.getElementById("promotionTitle").value = promotion.title || "";
    document.getElementById("promotionDescription").value = promotion.description || "";
    document.getElementById("promotionDiscount").value = promotion.discountPercent ?? "";
    document.getElementById("promotionStartDate").value = promotion.startDate || "";
    document.getElementById("promotionEndDate").value = promotion.endDate || "";

    const saveButton = document.getElementById("promotionSaveButton");
    const cancelButton = document.getElementById("promotionCancelButton");

    if (saveButton) saveButton.textContent = "Actualizar promoción";
    if (cancelButton) cancelButton.style.display = "inline-flex";

    document.getElementById("promotionTitle").focus();
}

async function togglePromotion(id) {
    const promotion = promotionsData.find(item => Number(item.id) === Number(id));

    if (!promotion) return;

    const confirm = await Swal.fire({
        icon: "question",
        title: promotion.active ? "¿Desactivar promoción?" : "¿Activar promoción?",
        text: promotion.active
            ? "La promoción dejará de mostrarse en el portal público."
            : "La promoción podrá mostrarse en el portal si está vigente.",
        showCancelButton: true,
        confirmButtonText: promotion.active ? "Sí, desactivar" : "Sí, activar",
        cancelButtonText: "Cancelar"
    });

    if (!confirm.isConfirmed) return;

    try {
        const response = await authFetch(`${baseUrl}/promotions/${id}/toggle`, {
            method: "PUT"
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            Swal.fire("Error", result.message || "No se pudo cambiar el estado.", "error");
            return;
        }

        await loadPromotions();

        Swal.fire({
            icon: "success",
            title: "Estado actualizado",
            timer: 1400,
            showConfirmButton: false
        });

    } catch (error) {
        console.error("Error cambiando promoción:", error);
        Swal.fire("Error", "Error de conexión.", "error");
    }
}

function clearPromotionForm() {
    editingPromotionId = null;

    document.getElementById("promotionTitle").value = "";
    document.getElementById("promotionDescription").value = "";
    document.getElementById("promotionDiscount").value = "";
    document.getElementById("promotionStartDate").value = "";
    document.getElementById("promotionEndDate").value = "";

    const saveButton = document.getElementById("promotionSaveButton");
    const cancelButton = document.getElementById("promotionCancelButton");

    if (saveButton) saveButton.textContent = "Guardar promoción";
    if (cancelButton) cancelButton.style.display = "none";
}

function escapePromotionHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}