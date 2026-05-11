let selectedPatientId = null;

async function openClinicalHistory(patientId, patientName) {
    selectedPatientId = patientId;

    document.getElementById("clinicalPatientName").textContent = patientName;
    document.getElementById("clinicalHistoryModal").classList.remove("hidden");

    await loadClinicalHistory(patientId);
}

function closeClinicalHistory() {
    document.getElementById("clinicalHistoryModal").classList.add("hidden");
}

async function saveClinicalHistory() {
    const data = {
        reason: document.getElementById("clinicalReason").value,
        diagnosis: document.getElementById("clinicalDiagnosis").value,
        evolution: document.getElementById("clinicalEvolution").value,
        recommendations: document.getElementById("clinicalRecommendations").value,
        psychologistName: `${currentUser.firstName} ${currentUser.lastName}`
    };

    if (!data.reason.trim()) {
        Swal.fire("Validación", "Ingresa el motivo de consulta", "warning");
        return;
    }

    const response = await authFetch(`${baseUrl}/clinical-history/patient/${selectedPatientId}`, {
        method: "POST",
        body: JSON.stringify(data)
    });

    if (!response || !response.ok) {
        Swal.fire("Error", "No se pudo guardar la historia clínica", "error");
        return;
    }

    Swal.fire("Correcto", "Historia clínica guardada", "success");

    clearClinicalForm();
    await loadClinicalHistory(selectedPatientId);
}

async function loadClinicalHistory(patientId) {
    const response = await authFetch(`${baseUrl}/clinical-history/patient/${patientId}`);
    if (!response || !response.ok) return;

    const data = await response.json();
    const container = document.getElementById("clinicalHistoryList");

    container.innerHTML = "";

    if (data.length === 0) {
        container.innerHTML = `<div class="empty-state">No hay registros clínicos.</div>`;
        return;
    }

    data.forEach(item => {
        const date = item.date
            ? item.date.replace("T", " ").substring(0, 16)
            : "Fecha no registrada";

        container.innerHTML += `
            <div class="clinical-timeline-item">
                <div class="clinical-timeline-dot"></div>

                <div class="clinical-timeline-card">
                    <div class="clinical-card-header">
                        <div>
                            <strong>${date}</strong>
                            <span>${item.psychologistName ?? "Psicólogo no registrado"}</span>
                        </div>

                        <button class="btn-danger-soft" onclick="deleteClinicalHistory(${item.id})">
                            Eliminar
                        </button>
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
}

async function deleteClinicalHistory(id) {
    const confirm = await Swal.fire({
        title: "¿Eliminar registro?",
        text: "Este registro dejará de mostrarse en la historia clínica.",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Sí, eliminar",
        cancelButtonText: "Cancelar"
    });

    if (!confirm.isConfirmed) return;

    const response = await authFetch(`${baseUrl}/clinical-history/${id}`, {
        method: "DELETE"
    });

    if (!response || !response.ok) {
        Swal.fire("Error", "No se pudo eliminar el registro", "error");
        return;
    }

    Swal.fire("Eliminado", "Registro clínico eliminado correctamente", "success");

    await loadClinicalHistory(selectedPatientId);
}

function clearClinicalForm() {
    document.getElementById("clinicalReason").value = "";
    document.getElementById("clinicalDiagnosis").value = "";
    document.getElementById("clinicalEvolution").value = "";
    document.getElementById("clinicalRecommendations").value = "";
}