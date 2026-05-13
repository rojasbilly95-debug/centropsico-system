let selectedPublicSlot = {
    psychologistId: null,
    psychologistName: "",
    date: "",
    time: ""
};

let selectedServiceData = null;

const ADVANCE_PERCENT = 20;

window.addEventListener("DOMContentLoaded", () => {
    loadPublicPlans();
    loadPublicServices();
    initPortalEffects();
});

async function loadPublicPlans() {
    const container = document.getElementById("plansContainer");
    if (!container) return;

    try {
        const response = await fetch("/api/public/plans");
        const plans = await response.json();

        if (!response.ok) {
            container.innerHTML = `<div class="loading-card">No se pudieron cargar los servicios.</div>`;
            return;
        }

        container.innerHTML = "";

        plans.forEach((plan, index) => {
            container.innerHTML += `
                <article class="plan-card ${index === 1 ? "featured-plan" : ""}">
                    ${index === 1 ? `<span class="plan-tag">Servicio solicitado</span>` : ""}
                    <h3>${plan.name}</h3>
                    <p>${plan.description}</p>
                    <div class="plan-price">Desde S/ ${Number(plan.price).toFixed(2)}</div>
                    <ul>
                        ${plan.features.map(feature => `<li>${feature}</li>`).join("")}
                    </ul>
                    <a href="#quote" class="plan-action">Solicitar orientación</a>
                </article>
            `;
        });

    } catch (error) {
        container.innerHTML = `<div class="loading-card">Error de conexión al cargar servicios.</div>`;
    }
}

async function submitLead(event) {
    event.preventDefault();

    const data = {
        fullName: getValue("leadFullName"),
        email: getValue("leadEmail"),
        phone: getValue("leadPhone"),

        serviceId: getSelectedServiceId(),
        serviceInterest: getSelectedServiceName(),

        modality: getValue("leadModality"),

        psychologistId: selectedPublicSlot.psychologistId,
        psychologistName: selectedPublicSlot.psychologistName,

        preferredDate: selectedPublicSlot.date,
        preferredTime: selectedPublicSlot.time,

        servicePrice: selectedServiceData?.price || 0,
        advancePercent: selectedServiceData?.advancePercent || 0,
        advanceAmount: selectedServiceData?.advanceAmount || 0,

        paymentMethod: getValue("leadPaymentMethod"),
        operationCode: getValue("leadOperationCode"),
        message: getValue("leadMessage")
    };

    const validationMessage = validateLeadForm(data);

    if (validationMessage) {
        showQuoteResult(validationMessage, false);
        return;
    }

    const submitButton = event.target.querySelector("button[type='submit']");
    const originalText = submitButton.textContent;

    try {
        submitButton.disabled = true;
        submitButton.textContent = "Enviando solicitud...";

        const response = await fetch("/api/public/leads", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(data)
        });

        const result = await response.json();

        if (!response.ok) {
            showQuoteResult(result.message || "No se pudo registrar la solicitud.", false);
            return;
        }

        showQuoteResult(
            `
            <strong>Solicitud enviada correctamente.</strong><br>
            Gracias por contactarnos. Nuestro equipo revisará tu solicitud y podrá comunicarse contigo para orientarte sobre el servicio más adecuado.<br>
            <small>Código de solicitud: #${result.leadId}</small>
            `,
            true
        );

        event.target.reset();

        selectedPublicSlot = {
            psychologistId: null,
            psychologistName: "",
            date: "",
            time: ""
        };

        selectedServiceData = null;

        document.getElementById("publicAvailabilityResult").innerHTML = "";
        document.getElementById("advancePaymentInfo").innerHTML = "";

    } catch (error) {
        showQuoteResult("Error de conexión. Inténtalo nuevamente.", false);
    } finally {
        submitButton.disabled = false;
        submitButton.textContent = originalText;
    }
}

function validateLeadForm(data) {
    if (!data.fullName) return "Ingresa tu nombre completo.";
    if (!data.email) return "Ingresa un correo electrónico.";
    if (!isValidEmail(data.email)) return "Ingresa un correo electrónico válido.";
    if (!data.phone) return "Ingresa un teléfono o WhatsApp.";
    if (!data.serviceInterest) return "Selecciona el tipo de atención que necesitas.";
    if (!data.modality) return "Selecciona una modalidad de atención.";
    if (!data.paymentMethod)
        return "Selecciona un método de pago.";
    if (!data.operationCode)
        return "Ingresa el código de operación del adelanto.";
    return null;
}

function showQuoteResult(message, success) {
    const resultBox = document.getElementById("quoteResult");
    if (!resultBox) return;

    resultBox.style.display = "block";
    resultBox.innerHTML = message;

    resultBox.classList.remove("quote-success", "quote-error");
    resultBox.classList.add(success ? "quote-success" : "quote-error");

    resultBox.scrollIntoView({
        behavior: "smooth",
        block: "center"
    });
}

function getValue(id) {
    return document.getElementById(id)?.value.trim() || "";
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function initPortalEffects() {
    const revealElements = document.querySelectorAll(".reveal");

    if (!("IntersectionObserver" in window)) {
        revealElements.forEach(el => el.classList.add("visible"));
        return;
    }

    const observer = new IntersectionObserver(entries => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add("visible");
                observer.unobserve(entry.target);
            }
        });
    }, {
        threshold: 0.16
    });

    revealElements.forEach(el => observer.observe(el));
}

async function loadPublicServices() {
    const select = document.getElementById("leadServiceInterest");
    if (!select) return;

    try {
        const response = await fetch("/api/public/services");
        const services = await response.json();

        if (!response.ok) return;

        select.innerHTML = `<option value="">Tipo de atención que necesitas</option>`;

        services.forEach(service => {
            select.innerHTML += `
                <option 
    value="${service.id}"
    data-name="${service.name}"
    data-price="${service.price}">
                    ${service.name}
                </option>
            `;
        });

        select.innerHTML += `
            <option value="NO_SEGURO" data-name="No estoy seguro">
                No estoy seguro, deseo orientación
            </option>
        `;

    } catch (error) {
        console.error("Error cargando servicios públicos:", error);
    }
}

function handleServiceSelection() {

    const select = document.getElementById("leadServiceInterest");

    if (!select) return;

    const option = select.options[select.selectedIndex];

    const price = Number(option?.dataset?.price || 0);

    selectedServiceData = {
        name: option?.dataset?.name || "",
        price: price,
        advancePercent: ADVANCE_PERCENT,
        advanceAmount: Number((price * (ADVANCE_PERCENT / 100)).toFixed(2))
    };

    renderAdvanceInfo();
}

async function loadPublicAvailability() {
    const serviceSelect = document.getElementById("leadServiceInterest");
    const dateInput = document.getElementById("availabilityDate");
    const result = document.getElementById("publicAvailabilityResult");

    if (!serviceSelect || !dateInput || !result) return;

    const serviceId = serviceSelect.value;
    const date = dateInput.value;

    result.innerHTML = "";

    if (!serviceId || serviceId === "NO_SEGURO") {
        result.innerHTML = `
            <div class="availability-message">
                Selecciona un servicio específico para ver horarios disponibles.
            </div>
        `;
        return;
    }

    if (!date) {
        result.innerHTML = `
            <div class="availability-message">
                Selecciona una fecha para consultar disponibilidad.
            </div>
        `;
        return;
    }

    try {
        result.innerHTML = `
            <div class="availability-message">
                Buscando horarios disponibles...
            </div>
        `;

        const response = await fetch(`/api/public/availability?serviceId=${serviceId}&date=${date}`);
        const data = await response.json();

        if (!response.ok) {
            result.innerHTML = `
                <div class="availability-message error">
                    No se pudo consultar la disponibilidad.
                </div>
            `;
            return;
        }

        renderPublicAvailability(data);

    } catch (error) {
        result.innerHTML = `
            <div class="availability-message error">
                Error de conexión al consultar horarios.
            </div>
        `;
    }
}

function renderPublicAvailability(data) {
    const result = document.getElementById("publicAvailabilityResult");
    if (!result) return;

    if (!data || data.length === 0) {
        result.innerHTML = `
            <div class="availability-message">
                No hay horarios disponibles para la fecha seleccionada.
            </div>
        `;
        return;
    }

    result.innerHTML = "";

    data.forEach(item => {
        result.innerHTML += `
            <div class="availability-card">
                <div>
                    <strong>${item.psychologistName}</strong>
                    <span>${item.specialty || "Psicología"}</span>
                </div>

                <div class="availability-slots">
                    ${item.slots.map(slot => `
                        <button type="button" onclick="selectPublicSlot(${item.psychologistId}, '${item.psychologistName}', '${slot}')">
                            ${slot}
                        </button>
                    `).join("")}
                </div>
            </div>
        `;
    });
}

function renderAdvanceInfo() {

    const container = document.getElementById("advancePaymentInfo");

    if (!container) return;

    if (!selectedServiceData || !selectedServiceData.price) {

        container.innerHTML = `
            <div class="advance-placeholder">
                Selecciona un servicio para visualizar el adelanto requerido.
            </div>
        `;

        return;
    }

    container.innerHTML = `
        <div class="advance-card">

            <div class="advance-header">
                <strong>Separación de atención</strong>
                <span>Adelanto requerido</span>
            </div>

            <div class="advance-values">

                <div>
                    <small>Precio referencial</small>
                    <strong>S/ ${selectedServiceData.price.toFixed(2)}</strong>
                </div>

                <div>
                    <small>Adelanto (${selectedServiceData.advancePercent}%)</small>
                    <strong>
                        S/ ${selectedServiceData.advanceAmount.toFixed(2)}
                    </strong>
                </div>

            </div>

            <div class="advance-payment-box">

                <strong>Yape / Transferencia</strong>

                <p>
                    Realiza el adelanto para separar tu horario de atención.
                </p>

                <div class="payment-number">
                    999 999 999
                </div>

            </div>

        </div>
    `;
}

function selectPublicSlot(psychologistId, psychologistName, slot) {
    const date = document.getElementById("availabilityDate")?.value || "";
    const message = document.getElementById("leadMessage");

    selectedPublicSlot = {
        psychologistId: psychologistId,
        psychologistName: psychologistName,
        date: date,
        time: slot
    };

    if (message) {
        message.value = `Deseo pre-reservar una atención con ${psychologistName} para el ${date} a las ${slot}.`;
    }

    document.querySelectorAll(".availability-slots button").forEach(btn => {
        btn.classList.remove("selected");
    });

    event.target.classList.add("selected");
}

function getSelectedServiceName() {
    const select = document.getElementById("leadServiceInterest");
    if (!select) return "";

    const option = select.options[select.selectedIndex];
    return option?.dataset?.name || option?.textContent?.trim() || "";
}

function getSelectedServiceId() {
    const select = document.getElementById("leadServiceInterest");

    if (!select || select.value === "NO_SEGURO") {
        return null;
    }

    return Number(select.value);
}

function togglePortalMenu() {
    const nav = document.querySelector(".portal-nav");
    if (!nav) return;

    nav.classList.toggle("open");
}

document.addEventListener("click", function (event) {
    const nav = document.querySelector(".portal-nav");
    const button = document.querySelector(".portal-menu-btn");

    if (!nav || !button) return;

    if (!nav.contains(event.target) && !button.contains(event.target)) {
        nav.classList.remove("open");
    }
});