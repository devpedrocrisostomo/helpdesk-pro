const API_BASE = "/api";

const state = {
    token: localStorage.getItem("helpdesk.token"),
    user: null,
    view: "dashboard",
    users: [],
    clients: [],
    tickets: [],
    comments: [],
    dashboard: null,
    selectedTicketId: null,
    editingUserId: null,
    editingClientId: null,
    editingTicketId: null,
    message: null
};

const app = document.getElementById("app");

document.addEventListener("DOMContentLoaded", init);

async function init() {
    if (!state.token) {
        renderLogin();
        return;
    }

    try {
        state.user = await api("/auth/me");
        await loadData();
    } catch (error) {
        localStorage.removeItem("helpdesk.token");
        state.token = null;
        state.user = null;
        state.message = { type: "error", text: error.message };
        renderLogin();
    }
}

async function api(path, options = {}) {
    const headers = {
        ...(options.headers || {})
    };

    if (state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    let body = options.body;
    if (body && typeof body === "object") {
        headers["Content-Type"] = "application/json";
        body = JSON.stringify(body);
    }

    const response = await fetch(`${API_BASE}${path}`, {
        method: options.method || "GET",
        headers,
        body
    });

    if (response.status === 204) {
        return null;
    }

    const text = await response.text();
    const data = text ? JSON.parse(text) : null;

    if (!response.ok) {
        const details = data && Array.isArray(data.details) ? ` ${data.details.join(" ")}` : "";
        throw new Error(`${data && data.message ? data.message : "Erro na requisicao."}${details}`);
    }

    return data;
}

async function loadData() {
    normalizeView();
    const [dashboard, clients, tickets] = await Promise.all([
        api("/tickets/dashboard"),
        api("/clients"),
        api("/tickets")
    ]);

    state.dashboard = dashboard;
    state.users = canManageUsers() ? await api("/users") : [];
    state.clients = clients;
    state.tickets = tickets;

    if (!state.selectedTicketId && tickets.length > 0) {
        state.selectedTicketId = tickets[0].id;
    }

    await loadComments();
    renderApp();
}

async function loadComments() {
    if (!state.selectedTicketId) {
        state.comments = [];
        return;
    }
    state.comments = await api(`/tickets/${state.selectedTicketId}/comments`);
}

function setMessage(type, text) {
    state.message = { type, text };
}

function clearMessage() {
    state.message = null;
}

function renderLogin() {
    app.innerHTML = `
        <main class="login-page">
            <section class="login-brand">
                <img class="login-logo" src="/assets/helpdesk-pro-logo.png" alt="HelpDesk Pro">
                <h1>Atendimento eficiente. Solucoes inteligentes.</h1>
                <p>Operacao de chamados, clientes e equipe tecnica em um painel direto para atendimento.</p>
            </section>
            <section class="login-panel">
                <h2>Entrar</h2>
                ${renderMessage()}
                <form id="loginForm" class="form-grid">
                    <label>E-mail
                        <input name="email" type="email" value="admin@helpdeskpro.local" required>
                    </label>
                    <label>Senha
                        <input name="password" type="password" value="123456" required>
                    </label>
                    <button class="btn primary" type="submit">Entrar</button>
                </form>
            </section>
        </main>
    `;

    document.getElementById("loginForm").addEventListener("submit", handleLogin);
}

function renderApp() {
    normalizeView();
    app.innerHTML = `
        <div class="shell">
            <aside class="sidebar">
                <div class="brand-row">
                    <img class="brand-logo" src="/assets/helpdesk-pro-logo.png" alt="HelpDesk Pro">
                    <h1 class="app-title">HelpDesk Pro</h1>
                </div>
                <nav class="nav">
                    ${navButton("dashboard", "Dashboard")}
                    ${navButton("tickets", "Chamados")}
                    ${canViewClientsPage() ? navButton("clients", "Clientes") : ""}
                    ${canManageUsers() ? navButton("users", "Usuarios") : ""}
                </nav>
                <div class="sidebar-foot">
                    <div>${escapeHtml(state.user.name)}</div>
                    <div>${escapeHtml(state.user.email)}</div>
                </div>
            </aside>
            <main class="main">
                <header class="topbar">
                    <div>
                        <h2>${viewTitle()}</h2>
                        <div class="muted">${summaryText()}</div>
                    </div>
                    <div class="topbar-actions">
                        <button class="btn" id="refreshButton" type="button">Atualizar</button>
                        <button class="btn danger" id="logoutButton" type="button">Sair</button>
                    </div>
                </header>
                <section class="content">
                    ${renderMessage()}
                    ${renderView()}
                </section>
            </main>
        </div>
    `;

    bindShellEvents();
    bindViewEvents();
}

function navButton(view, label) {
    return `<button class="${state.view === view ? "active" : ""}" data-view="${view}" type="button">${label}</button>`;
}

function viewTitle() {
    const titles = {
        dashboard: "Dashboard",
        tickets: "Chamados",
        clients: "Clientes",
        users: "Usuarios"
    };
    return titles[state.view] || "Dashboard";
}

function summaryText() {
    const parts = [`${state.tickets.length} chamados`];
    if (!isClientUser()) {
        parts.push(`${state.clients.length} clientes`);
    }
    if (canManageUsers()) {
        parts.push(`${state.users.length} usuarios`);
    }
    parts.push(`perfil ${state.user.role}`);
    return parts.join(", ");
}

function normalizeView() {
    if (state.view === "users" && !canManageUsers()) {
        state.view = "dashboard";
    }
    if (state.view === "clients" && !canViewClientsPage()) {
        state.view = "dashboard";
    }
}

function isAdminUser() {
    return state.user && state.user.role === "ADMIN";
}

function isTechnicianUser() {
    return state.user && state.user.role === "TECHNICIAN";
}

function isClientUser() {
    return state.user && state.user.role === "CLIENT";
}

function canManageUsers() {
    return isAdminUser();
}

function canViewClientsPage() {
    return isAdminUser() || isTechnicianUser();
}

function canManageClients() {
    return isAdminUser();
}

function canCreateTickets() {
    return Boolean(state.user);
}

function canEditTickets() {
    return isAdminUser() || isTechnicianUser();
}

function canDeleteTickets() {
    return isAdminUser();
}

function canAssignTickets() {
    return isAdminUser();
}

function canChangeTicketStatus() {
    return isAdminUser() || isTechnicianUser();
}

function canDeleteComment(comment) {
    return isAdminUser() || (state.user && comment.author && comment.author.id === state.user.id);
}

function renderView() {
    if (state.view === "tickets") return renderTickets();
    if (state.view === "clients" && canViewClientsPage()) return renderClients();
    if (state.view === "users" && canManageUsers()) return renderUsers();
    return renderDashboard();
}

function renderDashboard() {
    const dashboard = state.dashboard || { total: 0, byStatus: {}, byPriority: {} };
    return `
        <div class="grid">
            <div class="metrics">
                <div class="metric"><span>Total</span><strong>${dashboard.total || 0}</strong></div>
                <div class="metric"><span>Abertos</span><strong>${dashboard.byStatus.OPEN || 0}</strong></div>
                <div class="metric"><span>Em andamento</span><strong>${dashboard.byStatus.IN_PROGRESS || 0}</strong></div>
                <div class="metric"><span>Alta prioridade</span><strong>${dashboard.byPriority.HIGH || 0}</strong></div>
            </div>
            <div class="panel">
                <div class="panel-header">
                    <h3>Chamados recentes</h3>
                    <button class="btn primary" data-view="tickets" type="button">Abrir chamados</button>
                </div>
                <div class="table-wrap">
                    ${ticketTable(state.tickets.slice(0, 6), false)}
                </div>
            </div>
        </div>
    `;
}

function renderUsers() {
    const editing = state.users.find((user) => user.id === state.editingUserId);
    return `
        <div class="grid two">
            <section class="panel">
                <div class="panel-header">
                    <h3>${editing ? "Editar usuario" : "Novo usuario"}</h3>
                    ${editing ? '<button class="btn" id="cancelUserEdit" type="button">Cancelar</button>' : ""}
                </div>
                <div class="panel-body">
                    <form id="userForm" class="form-grid">
                        <label>Nome
                            <input name="name" value="${escapeAttr(editing ? editing.name : "")}" required maxlength="120">
                        </label>
                        <label>E-mail
                            <input name="email" type="email" value="${escapeAttr(editing ? editing.email : "")}" required maxlength="180">
                        </label>
                        <div class="form-row">
                            <label>Perfil
                                <select name="role" required>
                                    ${option("ADMIN", "ADMIN", editing && editing.role === "ADMIN")}
                                    ${option("TECHNICIAN", "TECHNICIAN", editing && editing.role === "TECHNICIAN")}
                                    ${option("CLIENT", "CLIENT", editing && editing.role === "CLIENT")}
                                </select>
                            </label>
                            <label>${editing ? "Nova senha" : "Senha"}
                                <input name="password" type="password" ${editing ? "" : "required"} minlength="6" maxlength="72">
                            </label>
                        </div>
                        <button class="btn primary" type="submit">${editing ? "Salvar usuario" : "Criar usuario"}</button>
                    </form>
                </div>
            </section>
            <section class="panel">
                <div class="panel-header"><h3>Usuarios cadastrados</h3></div>
                <div class="table-wrap">
                    <table>
                        <thead><tr><th>Nome</th><th>E-mail</th><th>Perfil</th><th>Acoes</th></tr></thead>
                        <tbody>
                            ${state.users.map((user) => `
                                <tr>
                                    <td>${escapeHtml(user.name)}</td>
                                    <td>${escapeHtml(user.email)}</td>
                                    <td><span class="tag">${escapeHtml(user.role)}</span></td>
                                    <td class="actions">
                                        <button class="btn" data-edit-user="${user.id}" type="button">Editar</button>
                                        <button class="btn danger" data-delete-user="${user.id}" type="button">Remover</button>
                                    </td>
                                </tr>
                            `).join("") || '<tr><td colspan="4"><div class="empty">Nenhum usuario cadastrado.</div></td></tr>'}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    `;
}

function renderClients() {
    const editing = state.clients.find((client) => client.id === state.editingClientId);
    return `
        <div class="grid two">
            ${canManageClients() ? `<section class="panel">
                <div class="panel-header">
                    <h3>${editing ? "Editar cliente" : "Novo cliente"}</h3>
                    ${editing ? '<button class="btn" id="cancelClientEdit" type="button">Cancelar</button>' : ""}
                </div>
                <div class="panel-body">
                    <form id="clientForm" class="form-grid">
                        <label>Nome
                            <input name="name" value="${escapeAttr(editing ? editing.name : "")}" required maxlength="140">
                        </label>
                        <label>E-mail
                            <input name="email" type="email" value="${escapeAttr(editing ? editing.email : "")}" required maxlength="180">
                        </label>
                        <div class="form-row">
                            <label>Telefone
                                <input name="phone" value="${escapeAttr(editing && editing.phone ? editing.phone : "")}" maxlength="30">
                            </label>
                            <label>Documento
                                <input name="document" value="${escapeAttr(editing ? editing.document : "")}" required maxlength="40">
                            </label>
                        </div>
                        <button class="btn primary" type="submit">${editing ? "Salvar cliente" : "Criar cliente"}</button>
                    </form>
                </div>
            </section>` : ""}
            <section class="panel">
                <div class="panel-header"><h3>Clientes cadastrados</h3></div>
                <div class="table-wrap">
                    <table>
                        <thead><tr><th>Nome</th><th>E-mail</th><th>Documento</th><th>Acoes</th></tr></thead>
                        <tbody>
                            ${state.clients.map((client) => `
                                <tr>
                                    <td>${escapeHtml(client.name)}</td>
                                    <td>${escapeHtml(client.email)}</td>
                                    <td>${escapeHtml(client.document)}</td>
                                    <td class="actions">
                                        ${canManageClients() ? `
                                        <button class="btn" data-edit-client="${client.id}" type="button">Editar</button>
                                        <button class="btn danger" data-delete-client="${client.id}" type="button">Remover</button>
                                        ` : '<span class="muted">Somente leitura</span>'}
                                    </td>
                                </tr>
                            `).join("") || '<tr><td colspan="4"><div class="empty">Nenhum cliente cadastrado.</div></td></tr>'}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    `;
}

function renderTickets() {
    const editing = state.tickets.find((ticket) => ticket.id === state.editingTicketId);
    const selected = state.tickets.find((ticket) => ticket.id === state.selectedTicketId);
    const selectedClientId = editing && editing.client ? editing.client.id : (state.clients[0] ? state.clients[0].id : "");
    return `
        <div class="grid two">
            <section class="grid">
                ${canCreateTickets() || editing ? `<div class="panel">
                    <div class="panel-header">
                        <h3>${editing ? "Editar chamado" : "Novo chamado"}</h3>
                        ${editing ? '<button class="btn" id="cancelTicketEdit" type="button">Cancelar</button>' : ""}
                    </div>
                    <div class="panel-body">
                        <form id="ticketForm" class="form-grid">
                            <label>Titulo
                                <input name="title" value="${escapeAttr(editing ? editing.title : "")}" required maxlength="180">
                            </label>
                            <label>Descricao
                                <textarea name="description" required>${escapeHtml(editing ? editing.description : "")}</textarea>
                            </label>
                            <div class="form-row">
                                <label>Prioridade
                                    <select name="priority" required>
                                        ${option("LOW", "LOW", editing && editing.priority === "LOW")}
                                        ${option("MEDIUM", "MEDIUM", editing && editing.priority === "MEDIUM")}
                                        ${option("HIGH", "HIGH", editing && editing.priority === "HIGH")}
                                    </select>
                                </label>
                                ${isClientUser() ? `<input type="hidden" name="clientId" value="${escapeAttr(String(selectedClientId))}">` : `<label>Cliente
                                    <select name="clientId" required>
                                        <option value="">Selecione</option>
                                        ${state.clients.map((client) => option(client.id, client.name, editing && editing.client.id === client.id)).join("")}
                                    </select>
                                </label>`}
                            </div>
                            ${canAssignTickets() ? `<label>Tecnico
                                <select name="assignedTechnicianId">
                                    <option value="">Sem tecnico</option>
                                    ${assignableUsers().map((user) => option(user.id, `${user.name} (${user.role})`, editing && editing.assignedTechnician && editing.assignedTechnician.id === user.id)).join("")}
                                </select>
                            </label>` : ""}
                            <button class="btn primary" type="submit">${editing ? "Salvar chamado" : "Criar chamado"}</button>
                        </form>
                    </div>
                </div>` : ""}
                <div class="panel">
                    <div class="panel-header"><h3>Fila de chamados</h3></div>
                    <div class="panel-body">
                        <div class="ticket-list">
                            ${state.tickets.map((ticket) => ticketItem(ticket)).join("") || '<div class="empty">Nenhum chamado cadastrado.</div>'}
                        </div>
                    </div>
                </div>
            </section>
            <section class="panel">
                <div class="panel-header">
                    <h3>${selected ? escapeHtml(selected.title) : "Detalhes"}</h3>
                    ${selected && canDeleteTickets() ? `<button class="btn danger" data-delete-ticket="${selected.id}" type="button">Remover</button>` : ""}
                </div>
                <div class="panel-body">
                    ${selected ? ticketDetail(selected) : '<div class="empty">Selecione um chamado.</div>'}
                </div>
            </section>
        </div>
    `;
}

function ticketItem(ticket) {
    return `
        <button class="ticket-item ${state.selectedTicketId === ticket.id ? "active" : ""}" data-select-ticket="${ticket.id}" type="button">
            <span class="ticket-item-title">
                <span>${escapeHtml(ticket.title)}</span>
                ${statusTag(ticket.status)}
            </span>
            <span class="muted">${escapeHtml(ticket.client.name)} - ${formatDate(ticket.createdAt)}</span>
        </button>
    `;
}

function ticketDetail(ticket) {
    return `
        <div class="grid">
            ${canChangeTicketStatus() || canAssignTickets() ? `<div class="form-row">
                ${canChangeTicketStatus() ? `
                <label>Status
                    <select data-ticket-status="${ticket.id}">
                        ${option("OPEN", "OPEN", ticket.status === "OPEN")}
                        ${option("IN_PROGRESS", "IN_PROGRESS", ticket.status === "IN_PROGRESS")}
                        ${option("DONE", "DONE", ticket.status === "DONE")}
                        ${option("CANCELED", "CANCELED", ticket.status === "CANCELED")}
                    </select>
                </label>` : ""}
                ${canAssignTickets() ? `
                <label>Tecnico
                    <select data-ticket-assignment="${ticket.id}">
                        <option value="">Sem tecnico</option>
                        ${assignableUsers().map((user) => option(user.id, `${user.name} (${user.role})`, ticket.assignedTechnician && ticket.assignedTechnician.id === user.id)).join("")}
                    </select>
                </label>` : ""}
            </div>` : ""}
            <div>
                <div class="muted">Cliente</div>
                <strong>${escapeHtml(ticket.client.name)}</strong>
                <div class="muted">${escapeHtml(ticket.client.email)}</div>
            </div>
            <div>
                <div class="muted">Descricao</div>
                <p>${escapeHtml(ticket.description)}</p>
            </div>
            <div class="actions">
                ${canEditTickets() ? `<button class="btn" data-edit-ticket="${ticket.id}" type="button">Editar</button>` : ""}
                ${priorityTag(ticket.priority)}
                <span class="tag">${formatDate(ticket.updatedAt)}</span>
            </div>
            <div>
                <h3>Comentarios</h3>
                <div class="comment-list">
                    ${state.comments.map((comment) => `
                        <div class="comment">
                            <div class="comment-head">
                                <span>${escapeHtml(comment.author.name)} - ${formatDate(comment.createdAt)}</span>
                                ${canDeleteComment(comment) ? `<button class="btn link" data-delete-comment="${comment.id}" type="button">Remover</button>` : ""}
                            </div>
                            <div>${escapeHtml(comment.message)}</div>
                        </div>
                    `).join("") || '<div class="empty">Nenhum comentario neste chamado.</div>'}
                </div>
                <form id="commentForm" class="form-grid">
                    <label>Mensagem
                        <textarea name="message" required maxlength="5000"></textarea>
                    </label>
                    <button class="btn primary" type="submit">Adicionar comentario</button>
                </form>
            </div>
        </div>
    `;
}

function ticketTable(tickets, withActions) {
    return `
        <table>
            <thead><tr><th>Titulo</th><th>Cliente</th><th>Status</th><th>Prioridade</th>${withActions ? "<th>Acoes</th>" : ""}</tr></thead>
            <tbody>
                ${tickets.map((ticket) => `
                    <tr>
                        <td>${escapeHtml(ticket.title)}</td>
                        <td>${escapeHtml(ticket.client.name)}</td>
                        <td>${statusTag(ticket.status)}</td>
                        <td>${priorityTag(ticket.priority)}</td>
                        ${withActions ? `<td><button class="btn" data-select-ticket="${ticket.id}" type="button">Abrir</button></td>` : ""}
                    </tr>
                `).join("") || `<tr><td colspan="${withActions ? 5 : 4}"><div class="empty">Nenhum chamado cadastrado.</div></td></tr>`}
            </tbody>
        </table>
    `;
}

function renderMessage() {
    if (!state.message) return "";
    return `<div class="message ${state.message.type}">${escapeHtml(state.message.text)}</div>`;
}

function bindShellEvents() {
    document.querySelectorAll("[data-view]").forEach((button) => {
        button.addEventListener("click", async () => {
            state.view = button.dataset.view;
            clearMessage();
            renderApp();
        });
    });

    document.getElementById("refreshButton").addEventListener("click", async () => {
        await runAction(async () => {
            await loadData();
            setMessage("ok", "Dados atualizados.");
        });
    });

    document.getElementById("logoutButton").addEventListener("click", () => {
        localStorage.removeItem("helpdesk.token");
        state.token = null;
        state.user = null;
        renderLogin();
    });
}

function bindViewEvents() {
    const userForm = document.getElementById("userForm");
    if (userForm) userForm.addEventListener("submit", handleSaveUser);
    const cancelUserEdit = document.getElementById("cancelUserEdit");
    if (cancelUserEdit) cancelUserEdit.addEventListener("click", () => { state.editingUserId = null; renderApp(); });
    document.querySelectorAll("[data-edit-user]").forEach((button) => button.addEventListener("click", () => { state.editingUserId = Number(button.dataset.editUser); renderApp(); }));
    document.querySelectorAll("[data-delete-user]").forEach((button) => button.addEventListener("click", () => handleDeleteUser(Number(button.dataset.deleteUser))));

    const clientForm = document.getElementById("clientForm");
    if (clientForm) clientForm.addEventListener("submit", handleSaveClient);
    const cancelClientEdit = document.getElementById("cancelClientEdit");
    if (cancelClientEdit) cancelClientEdit.addEventListener("click", () => { state.editingClientId = null; renderApp(); });
    document.querySelectorAll("[data-edit-client]").forEach((button) => button.addEventListener("click", () => { state.editingClientId = Number(button.dataset.editClient); renderApp(); }));
    document.querySelectorAll("[data-delete-client]").forEach((button) => button.addEventListener("click", () => handleDeleteClient(Number(button.dataset.deleteClient))));

    const ticketForm = document.getElementById("ticketForm");
    if (ticketForm) ticketForm.addEventListener("submit", handleSaveTicket);
    const cancelTicketEdit = document.getElementById("cancelTicketEdit");
    if (cancelTicketEdit) cancelTicketEdit.addEventListener("click", () => { state.editingTicketId = null; renderApp(); });
    document.querySelectorAll("[data-edit-ticket]").forEach((button) => button.addEventListener("click", () => { state.editingTicketId = Number(button.dataset.editTicket); renderApp(); }));
    document.querySelectorAll("[data-delete-ticket]").forEach((button) => button.addEventListener("click", () => handleDeleteTicket(Number(button.dataset.deleteTicket))));
    document.querySelectorAll("[data-select-ticket]").forEach((button) => button.addEventListener("click", () => handleSelectTicket(Number(button.dataset.selectTicket))));
    document.querySelectorAll("[data-ticket-status]").forEach((select) => select.addEventListener("change", () => handleChangeStatus(Number(select.dataset.ticketStatus), select.value)));
    document.querySelectorAll("[data-ticket-assignment]").forEach((select) => select.addEventListener("change", () => handleAssignTicket(Number(select.dataset.ticketAssignment), select.value)));

    const commentForm = document.getElementById("commentForm");
    if (commentForm) commentForm.addEventListener("submit", handleSaveComment);
    document.querySelectorAll("[data-delete-comment]").forEach((button) => button.addEventListener("click", () => handleDeleteComment(Number(button.dataset.deleteComment))));
}

async function handleLogin(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await runAction(async () => {
        const auth = await api("/auth/login", {
            method: "POST",
            body: {
                email: form.get("email"),
                password: form.get("password")
            }
        });
        state.token = auth.token;
        state.user = auth.user;
        localStorage.setItem("helpdesk.token", auth.token);
        await loadData();
    }, renderLogin);
}

async function handleSaveUser(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const body = {
        name: form.get("name"),
        email: form.get("email"),
        role: form.get("role")
    };
    const password = form.get("password");
    if (password) body.password = password;

    await runAction(async () => {
        if (state.editingUserId) {
            await api(`/users/${state.editingUserId}`, { method: "PUT", body });
            setMessage("ok", "Usuario atualizado.");
        } else {
            await api("/users", { method: "POST", body });
            setMessage("ok", "Usuario criado.");
        }
        state.editingUserId = null;
        await loadData();
    });
}

async function handleDeleteUser(id) {
    if (!window.confirm("Remover este usuario?")) return;
    await runAction(async () => {
        await api(`/users/${id}`, { method: "DELETE" });
        setMessage("ok", "Usuario removido.");
        await loadData();
    });
}

async function handleSaveClient(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const body = {
        name: form.get("name"),
        email: form.get("email"),
        phone: form.get("phone"),
        document: form.get("document")
    };

    await runAction(async () => {
        if (state.editingClientId) {
            await api(`/clients/${state.editingClientId}`, { method: "PUT", body });
            setMessage("ok", "Cliente atualizado.");
        } else {
            await api("/clients", { method: "POST", body });
            setMessage("ok", "Cliente criado.");
        }
        state.editingClientId = null;
        await loadData();
    });
}

async function handleDeleteClient(id) {
    if (!window.confirm("Remover este cliente?")) return;
    await runAction(async () => {
        await api(`/clients/${id}`, { method: "DELETE" });
        setMessage("ok", "Cliente removido.");
        await loadData();
    });
}

async function handleSaveTicket(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const body = {
        title: form.get("title"),
        description: form.get("description"),
        priority: form.get("priority"),
        clientId: Number(form.get("clientId")),
        assignedTechnicianId: nullableNumber(form.get("assignedTechnicianId"))
    };

    await runAction(async () => {
        let ticket;
        if (state.editingTicketId) {
            ticket = await api(`/tickets/${state.editingTicketId}`, { method: "PUT", body });
            setMessage("ok", "Chamado atualizado.");
        } else {
            ticket = await api("/tickets", { method: "POST", body });
            setMessage("ok", "Chamado criado.");
        }
        state.editingTicketId = null;
        state.selectedTicketId = ticket.id;
        await loadData();
    });
}

async function handleDeleteTicket(id) {
    if (!window.confirm("Remover este chamado?")) return;
    await runAction(async () => {
        await api(`/tickets/${id}`, { method: "DELETE" });
        if (state.selectedTicketId === id) {
            state.selectedTicketId = null;
        }
        setMessage("ok", "Chamado removido.");
        await loadData();
    });
}

async function handleSelectTicket(id) {
    await runAction(async () => {
        state.selectedTicketId = id;
        await loadComments();
        renderApp();
    });
}

async function handleChangeStatus(id, status) {
    await runAction(async () => {
        await api(`/tickets/${id}/status`, { method: "PATCH", body: { status } });
        setMessage("ok", "Status atualizado.");
        await loadData();
    });
}

async function handleAssignTicket(id, technicianId) {
    await runAction(async () => {
        await api(`/tickets/${id}/assignment`, { method: "PATCH", body: { technicianId: nullableNumber(technicianId) } });
        setMessage("ok", "Atribuicao atualizada.");
        await loadData();
    });
}

async function handleSaveComment(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await runAction(async () => {
        await api(`/tickets/${state.selectedTicketId}/comments`, {
            method: "POST",
            body: { message: form.get("message") }
        });
        setMessage("ok", "Comentario adicionado.");
        await loadComments();
        renderApp();
    });
}

async function handleDeleteComment(commentId) {
    if (!window.confirm("Remover este comentario?")) return;
    await runAction(async () => {
        await api(`/tickets/${state.selectedTicketId}/comments/${commentId}`, { method: "DELETE" });
        setMessage("ok", "Comentario removido.");
        await loadComments();
        renderApp();
    });
}

async function runAction(action, fallbackRender = renderApp) {
    try {
        clearMessage();
        await action();
    } catch (error) {
        setMessage("error", error.message);
        fallbackRender();
    }
}

function assignableUsers() {
    return state.users.filter((user) => user.role === "ADMIN" || user.role === "TECHNICIAN");
}

function nullableNumber(value) {
    return value === null || value === undefined || value === "" ? null : Number(value);
}

function option(value, label, selected) {
    return `<option value="${escapeAttr(String(value))}" ${selected ? "selected" : ""}>${escapeHtml(String(label))}</option>`;
}

function statusTag(status) {
    const classes = {
        OPEN: "open",
        IN_PROGRESS: "progress",
        DONE: "done",
        CANCELED: ""
    };
    return `<span class="tag ${classes[status] || ""}">${escapeHtml(status)}</span>`;
}

function priorityTag(priority) {
    const classes = {
        LOW: "low",
        MEDIUM: "medium",
        HIGH: "high"
    };
    return `<span class="tag ${classes[priority] || ""}">${escapeHtml(priority)}</span>`;
}

function formatDate(value) {
    if (!value) return "-";
    return new Intl.DateTimeFormat("pt-BR", {
        dateStyle: "short",
        timeStyle: "short"
    }).format(new Date(value));
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function escapeAttr(value) {
    return escapeHtml(value);
}
