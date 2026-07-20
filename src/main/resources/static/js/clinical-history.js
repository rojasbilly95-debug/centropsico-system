let selectedPatientId = null;

async function openClinicalHistory(patientId, patientName) {
    selectedPatientId = patientId;

    document.getElementById("clinicalPatientName").textContent = patientName || "Paciente";
    document.getElementById("clinicalHistoryModal").classList.remove("hidden");

    clearClinicalForm();

    await loadClinicalHistory(patientId);
}

function closeClinicalHistory() {
    document.getElementById("clinicalHistoryModal").classList.add("hidden");
    selectedPatientId = null;
    clearClinicalForm();
}

async function saveClinicalHistory() {
    if (!selectedPatientId) {
        Swal.fire("Error", "No se seleccionó un paciente.", "error");
        return;
    }

    const data = {
        reason: document.getElementById("clinicalReason").value.trim(),
        diagnosis: document.getElementById("clinicalDiagnosis").value.trim(),
        evolution: document.getElementById("clinicalEvolution").value.trim(),
        recommendations: document.getElementById("clinicalRecommendations").value.trim(),
        psychologistName: `${currentUser.firstName} ${currentUser.lastName}`
    };

    if (!data.reason) {
        Swal.fire("Validación", "Ingresa el motivo de consulta.", "warning");
        return;
    }

    try {
        const response = await authFetch(`${baseUrl}/clinical-history/patient/${selectedPatientId}`, {
            method: "POST",
            body: JSON.stringify(data)
        });

        if (!response) return;

        let result = null;

        try {
            result = await response.json();
        } catch (e) {
            result = null;
        }

        if (!response.ok) {
            Swal.fire(
                "Error",
                result?.message || "No se pudo guardar la historia clínica.",
                "error"
            );
            return;
        }

        Swal.fire("Correcto", "Historia clínica guardada correctamente.", "success");

        clearClinicalForm();
        await loadClinicalHistory(selectedPatientId);

    } catch (error) {
        console.error("Error al guardar historia clínica:", error);
        Swal.fire("Error", "Error de conexión con el servidor.", "error");
    }
}

async function loadClinicalHistory(patientId) {
    const container = document.getElementById("clinicalHistoryList");

    if (!container) return;

    container.innerHTML = `
        <div class="empty-state">
            Cargando historia clínica...
        </div>
    `;

    try {
        const response = await authFetch(`${baseUrl}/clinical-history/patient/${patientId}`);

        if (!response) return;

        let data = null;

        try {
            data = await response.json();
        } catch (e) {
            data = null;
        }

        if (!response.ok) {
            container.innerHTML = `
                <div class="empty-state">
                    No se pudo cargar la historia clínica.
                </div>
            `;

            Swal.fire(
                "Acceso restringido",
                data?.message || "No tienes permiso para ver la historia clínica de este paciente.",
                "warning"
            );
            return;
        }

        container.innerHTML = "";

        if (!data || data.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    No hay registros clínicos.
                </div>
            `;
            return;
        }

        data.forEach(item => {
            const date = item.date
                ? item.date.replace("T", " ").substring(0, 16)
                : "Fecha no registrada";

            const canDeleteClinicalHistory = currentUser.role === "ADMIN";

            container.innerHTML += `
                <div class="clinical-timeline-item">
                    <div class="clinical-timeline-dot"></div>

                    <div class="clinical-timeline-card">
                        <div class="clinical-card-header">
                            <div>
                                <strong>${date}</strong>
                                <span>${item.psychologistName ?? "Psicólogo no registrado"}</span>
                            </div>

                            ${
                                canDeleteClinicalHistory
                                    ? `
                                        <button class="btn-danger-soft" onclick="deleteClinicalHistory(${item.id})">
                                            Eliminar
                                        </button>
                                    `
                                    : ""
                            }
                        </div>

                        <div class="clinical-card-body">
                            <p><b>Motivo:</b> ${item.reason ?? "-"}</p>
                            <p><b>Diagnóstico:</b> ${item.diagnosis ?? "-"}</p>
                            <p><b>Evolución:</b> ${item.evolution ?? "-"}</p>
                            <p><b>Recomendaciones:</b> ${item.recommendations ?? "-"}</p>
                        </div>
                    </div>
                </div>
            `;
        });

    } catch (error) {
        console.error("Error al cargar historia clínica:", error);

        container.innerHTML = `
            <div class="empty-state">
                Error al cargar historia clínica.
            </div>
        `;

        Swal.fire("Error", "Error de conexión con el servidor.", "error");
    }
}

async function deleteClinicalHistory(id) {
    if (currentUser.role !== "ADMIN") {
        Swal.fire(
            "Acceso denegado",
            "Solo el administrador puede eliminar registros clínicos.",
            "warning"
        );
        return;
    }

    const confirm = await Swal.fire({
        title: "¿Eliminar registro?",
        text: "Este registro dejará de mostrarse en la historia clínica.",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Sí, eliminar",
        cancelButtonText: "Cancelar"
    });

    if (!confirm.isConfirmed) return;

    try {
        const response = await authFetch(`${baseUrl}/clinical-history/${id}`, {
            method: "DELETE"
        });

        if (!response) return;

        if (!response.ok) {
            let result = null;

            try {
                result = await response.json();
            } catch (e) {
                result = null;
            }

            Swal.fire(
                "Error",
                result?.message || "No se pudo eliminar el registro.",
                "error"
            );
            return;
        }

        Swal.fire("Eliminado", "Registro clínico eliminado correctamente.", "success");

        await loadClinicalHistory(selectedPatientId);

    } catch (error) {
        console.error("Error al eliminar historia clínica:", error);
        Swal.fire("Error", "Error de conexión con el servidor.", "error");
    }
}

function clearClinicalForm() {
    document.getElementById("clinicalReason").value = "";
    document.getElementById("clinicalDiagnosis").value = "";
    document.getElementById("clinicalEvolution").value = "";
    document.getElementById("clinicalRecommendations").value = "";
}