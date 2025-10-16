document.addEventListener('DOMContentLoaded', () => {
    const routesApiBaseUrl = 'https://localhost:8765/api/v1/routes';
    const navigatorApiBaseUrl = 'https://localhost:8765/api/v1/navigator';

    const routesTbody = document.querySelector('#routes-table tbody');
    const routeForm = document.getElementById('route-form');
    const applyFiltersBtn = document.getElementById('apply-filters');
    const filtersContainer = document.getElementById('filters-container');
    const addFilterBtn = document.getElementById('add-filter');
    const clearFiltersBtn = document.getElementById('clear-filters');
    const sortFieldSelect = document.getElementById('sort-field');
    const sortOrderSelect = document.getElementById('sort-order');
    const navigatorOptimalForm = document.getElementById('navigator-optimal-form');
    const navigatorAddForm = document.getElementById('navigator-add-form');
    const btnMaxFrom = document.getElementById('btn-max-from');
    const btnUniqueDistances = document.getElementById('btn-unique-distances');
    // Modal helpers
    const modalOverlay = document.getElementById('modal-overlay');
    const modalTitleEl = document.getElementById('modal-title');
    const modalBodyEl = document.getElementById('modal-body');
    const modalOkBtn = document.getElementById('modal-ok');
    const modalCloseBtn = document.getElementById('modal-close');

    function showModal({ title = 'Message', message = '' }) {
        modalTitleEl.textContent = title;
        modalBodyEl.innerHTML = message;
        modalOverlay.classList.add('show');
        modalOverlay.setAttribute('aria-hidden', 'false');
    }

    function hideModal() {
        modalOverlay.classList.remove('show');
        modalOverlay.setAttribute('aria-hidden', 'true');
    }

    modalOkBtn.addEventListener('click', hideModal);
    modalOverlay.addEventListener('click', (e) => {
        if (e.target === modalOverlay) hideModal();
    });

    function parseXmlErrorMessage(xmlString) {
        try {
            const parser = new DOMParser();
            const xmlDoc = parser.parseFromString(xmlString, 'application/xml');
            const messageNode = xmlDoc.getElementsByTagName('message')[0];
            if (messageNode && messageNode.textContent) return messageNode.textContent;
            return null;
        } catch (_) {
            return null;
        }
    }


    let currentPage = 1;
    const pageSize = 10;

    async function fetchRoutes(page = 1, filters = {}, operations = {}, sort = 'id_asc') {
        const params = new URLSearchParams({
            page: page,
            pageSize: pageSize,
            sort: sort,
        });

        Object.entries(filters).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                params.append(key, value);
            }
        });
        Object.entries(operations).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                params.append(key, value);
            }
        });

        try {
            const response = await fetch(`${routesApiBaseUrl}?${params}`);
            if (!response.ok) {
                const errorXml = await response.text();
                throw new Error(`HTTP error! status: ${response.status} - ${errorXml}`);
            }

            const totalCount = response.headers.get('X-Total-Count');
            const xmlString = await response.text();
            const routes = parseRoutesXml(xmlString);
            
            renderRoutes(routes);
            renderPagination(totalCount, page, pageSize);

        } catch (error) {
            console.error('Failed to load routes:', error);
            showError('Failed to load routes. Please check connection to the service.');
        }
    }

    function parseRoutesXml(xmlString) {
        const parser = new DOMParser();
        const xmlDoc = parser.parseFromString(xmlString, "application/xml");
        const errorNode = xmlDoc.getElementsByTagName('Error')[0];
        if (errorNode) {
            const message = errorNode.getElementsByTagName('message')[0]?.textContent;
            throw new Error(message || 'Unknown server error');
        }

        return Array.from(xmlDoc.getElementsByTagName('Route')).map(routeNode => {
            const getVal = (context, tagName) => context.getElementsByTagName(tagName)[0]?.textContent || '';
            
            const coordinatesNode = routeNode.getElementsByTagName('coordinates')[0] || {};
            const fromNode = routeNode.getElementsByTagName('from')[0] || {};
            const toNode = routeNode.getElementsByTagName('to')[0] || {};

            return {
                id: getVal(routeNode, 'id'),
                name: getVal(routeNode, 'name'),
                coordinates: `(${getVal(coordinatesNode, 'x')}, ${getVal(coordinatesNode, 'y')})`,
                creationDate: new Date(getVal(routeNode, 'creationDate')).toLocaleString(),
                from: `${getVal(fromNode, 'name')} (${getVal(fromNode, 'x')}, ${getVal(fromNode, 'y')})`,
                to: `${getVal(toNode, 'name')} (${getVal(toNode, 'x')}, ${getVal(toNode, 'y')})`,
                distance: getVal(routeNode, 'distance'),
                priority: getVal(routeNode, 'priority')
            };
        });
    }

    function renderRoutes(routes) {
        routesTbody.innerHTML = '';
        if (routes.length === 0) {
            routesTbody.innerHTML = '<tr><td colspan="8">No routes found.</td></tr>';
            return;
        }
        routes.forEach(route => {
            const row = routesTbody.insertRow();
            row.innerHTML = `
                <td>${route.id}</td>
                <td>${route.name}</td>
                <td>${route.coordinates}</td>
                <td>${route.creationDate}</td>
                <td>${route.from}</td>
                <td>${route.to}</td>
                <td>${route.distance || 'N/A'}</td>
                <td>${route.priority || 'N/A'}</td>
                <td>
                    <button class="edit-btn" data-id="${route.id}">Edit</button>
                    <button class="delete-btn" data-id="${route.id}">Delete</button>
                </td>
            `;
        });
    }

    function renderPagination(totalItems, currentPage, pageSize) {
        const paginationContainer = document.querySelector('.pagination');
        paginationContainer.innerHTML = '';
        const totalPages = Math.ceil(totalItems / pageSize);

        for (let i = 1; i <= totalPages; i++) {
            const button = document.createElement('button');
            button.textContent = i;
            button.disabled = i === currentPage;
            button.addEventListener('click', () => {
                currentPage = i;
                applyFiltersAndSort();
            });
            paginationContainer.appendChild(button);
        }
    }

    function createFilterRow(initial = {}) {
        const row = document.createElement('div');
        row.className = 'filter-row';

        const fieldSelect = document.createElement('select');
        fieldSelect.className = 'filter-field';
        [
            ['name','Name'],
            ['distance','Distance'],
            ['priority','Priority'],
            ['coordinates.x','Coordinates X'],
            ['coordinates.y','Coordinates Y'],
            ['from.name','From Name'],
            ['from.x','From X'],
            ['from.y','From Y'],
            ['to.name','To Name'],
            ['to.x','To X'],
            ['to.y','To Y'],
            ['creationDate','Creation Date']
        ].forEach(([value,label]) => {
            const opt = document.createElement('option');
            opt.value = value; opt.textContent = label; fieldSelect.appendChild(opt);
        });

        const opSelect = document.createElement('select');
        opSelect.className = 'filter-op';

        const valueInput = document.createElement('input');
        valueInput.className = 'filter-value';

        const removeBtn = document.createElement('button');
        removeBtn.type = 'button';
        removeBtn.textContent = '×';
        removeBtn.addEventListener('click', () => row.remove());

        function setOpOptionsForField(field) {
            opSelect.innerHTML = '';
            let ops = ['==','!='];
            if (['name','from.name','to.name'].includes(field)) {
                ops = ['==','!=','~'];
            } else if (['priority'].includes(field)) {
                ops = ['==','!='];
            } else {
                ops = ['==','!=','>','>=','<','<='];
            }
            ops.forEach(o => { const opt=document.createElement('option'); opt.value=o; opt.textContent=o; opSelect.appendChild(opt); });
        }

        function setValueEditorForField(field) {
            valueInput.type = 'text';
            valueInput.value = '';
            if (['distance','coordinates.x','coordinates.y','from.x','from.y','to.x','to.y'].includes(field)) {
                valueInput.type = 'number';
            } else if (field === 'creationDate') {
                valueInput.type = 'datetime-local';
            } else if (field === 'priority') {
                const select = document.createElement('select');
                select.className = 'filter-value';
                ['LOW','MEDIUM','HIGH'].forEach(p => { const opt=document.createElement('option'); opt.value=p; opt.textContent=p; select.appendChild(opt); });
                row.replaceChild(select, valueInput);
                return;
            }
            // if previously replaced with select, ensure input is present
            if (!(valueInput.parentElement === row)) {
                row.appendChild(valueInput);
            }
        }

        fieldSelect.addEventListener('change', () => {
            setOpOptionsForField(fieldSelect.value);
            setValueEditorForField(fieldSelect.value);
        });

        row.appendChild(fieldSelect);
        row.appendChild(opSelect);
        row.appendChild(valueInput);
        row.appendChild(removeBtn);

        // initialize
        if (initial.field) fieldSelect.value = initial.field;
        setOpOptionsForField(fieldSelect.value);
        if (initial.op) opSelect.value = initial.op;
        setValueEditorForField(fieldSelect.value);
        if (initial.value) {
            const valEl = row.querySelector('.filter-value');
            valEl.value = initial.value;
        }

        return row;
    }

    function collectFilters() {
        const filters = {};
        const operations = {};
        const rows = Array.from(filtersContainer.querySelectorAll('.filter-row'));
        rows.forEach(row => {
            const field = row.querySelector('.filter-field').value;
            const op = row.querySelector('.filter-op').value;
            const valEl = row.querySelector('.filter-value');
            const value = valEl ? valEl.value : '';
            if (!value) return;
            filters[field] = value;
            const opKey = field.replace(/\./g, '.') + '_op';
            operations[opKey] = op;
        });
        return { filters, operations };
    }

    function applyFiltersAndSort() {
        const { filters, operations } = collectFilters();
        const sort = `${sortFieldSelect.value}_${sortOrderSelect.value}`;
        fetchRoutes(currentPage, filters, operations, sort);
    }
    
    addFilterBtn.addEventListener('click', () => {
        filtersContainer.appendChild(createFilterRow());
    });

    clearFiltersBtn.addEventListener('click', () => {
        filtersContainer.innerHTML = '';
        currentPage = 1;
        applyFiltersAndSort();
    });

    applyFiltersBtn.addEventListener('click', () => {
        currentPage = 1;
        applyFiltersAndSort();
    });

    // Extra actions handlers
    btnMaxFrom.addEventListener('click', async () => {
        try {
            const response = await fetch(`${routesApiBaseUrl}/max/from`);
            if (!response.ok) {
                const errorText = await response.text();
                const parsedMsg = parseXmlErrorMessage(errorText);
                const msg = parsedMsg || errorText;
                throw new Error(`Failed to get route: ${msg}`);
            }
            const xmlString = await response.text();
            const route = parseRoutesXml(xmlString)[0];
            if (!route) {
                showInfo('Route not found.');
                return;
            }
            showInfo(`Route with maximum FROM: <b>${route.name}</b><br>ID: ${route.id}<br>FROM: ${route.from}`);
        } catch (error) {
            showError(error.message);
        }
    });

    btnUniqueDistances.addEventListener('click', async () => {
        try {
            const response = await fetch(`${routesApiBaseUrl}/distances/unique`);
            if (!response.ok) {
                const errorText = await response.text();
                const parsedMsg = parseXmlErrorMessage(errorText);
                const msg = parsedMsg || errorText;
                throw new Error(`Failed to get unique distances: ${msg}`);
            }
            const xmlString = await response.text();
            const parser = new DOMParser();
            const xmlDoc = parser.parseFromString(xmlString, 'application/xml');
            const distances = Array.from(xmlDoc.getElementsByTagName('Distance')).map(n => n.textContent).filter(Boolean);
            showInfo(`Unique distances: ${distances.length ? distances.join(', ') : 'no data'}`);
        } catch (error) {
            showError(error.message);
        }
    });

    routesTbody.addEventListener('click', async (event) => {
        const target = event.target;
        const id = target.dataset.id;
        if (target.classList.contains('delete-btn')) {
            deleteRoute(id);
        }
        if (target.classList.contains('edit-btn')) {
            const route = await getRouteById(id);
            if(route) populateRouteForm(route);
        }
    });

    routeForm.addEventListener('submit', (event) => {
        event.preventDefault();
        saveRoute();
    });

    navigatorOptimalForm.addEventListener('submit', (event) => {
        event.preventDefault();
        findOptimalRoute();
    });

    navigatorAddForm.addEventListener('submit', (event) => {
        event.preventDefault();
        addNavigatorRoute();
    });

    async function getRouteById(id) {
        try {
            const response = await fetch(`${routesApiBaseUrl}/${id}`);
            if (!response.ok) throw new Error('Route not found');
            const xmlString = await response.text();
            return parseRoutesXml(xmlString)[0];
        } catch (error) {
            showError(error.message);
            return null;
        }
    }

    async function saveRoute() {
        const id = document.getElementById('route-id').value;
        const name = document.getElementById('route-name').value;
        const distance = document.getElementById('route-distance').value;
    
        const payload = `
            <Route>
                <name>${name}</name>
                <coordinates>
                    <x>${document.getElementById('route-coord-x').value}</x>
                    <y>${document.getElementById('route-coord-y').value}</y>
                </coordinates>
                <from>
                    <x>${document.getElementById('route-from-x').value}</x>
                    <y>${document.getElementById('route-from-y').value}</y>
                    <name>${document.getElementById('route-from-name').value}</name>
                </from>
                <to>
                    <x>${document.getElementById('route-to-x').value}</x>
                    <y>${document.getElementById('route-to-y').value}</y>
                    <name>${document.getElementById('route-to-name').value}</name>
                </to>
                ${distance ? `<distance>${distance}</distance>` : ''}
                ${document.getElementById('route-priority').value ? `<priority>${document.getElementById('route-priority').value}</priority>` : ''}
            </Route>
        `;
    
        const method = id ? 'PUT' : 'POST';
        const url = id ? `${routesApiBaseUrl}/${id}` : routesApiBaseUrl;
    
        try {
            const response = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/xml' },
                body: payload
            });
    
            if (!response.ok) {
                const errorText = await response.text();
                const parsedMsg = parseXmlErrorMessage(errorText);
                const msg = parsedMsg || errorText;
                throw new Error(`Failed to save route: ${msg}`);
            }
    
            routeForm.reset();
            fetchRoutes(currentPage);
        } catch (error) {
            showError(error.message);
        }
    }

    async function deleteRoute(id) {
        // confirmation via modal
        confirmModal({
            title: 'Delete confirmation',
            message: `Are you sure you want to delete route ${id}?`,
            onConfirm: async () => {
                try {
                    const response = await fetch(`${routesApiBaseUrl}/${id}`, { method: 'DELETE' });
                    if (!response.ok) {
                        throw new Error('Failed to delete route.');
                    }
                    fetchRoutes(currentPage);
                } catch (error) {
                    showError(error.message);
                }
            }
        });
        return;
    }

    async function findOptimalRoute() {
        const fromId = document.getElementById('nav-from-id').value;
        const toId = document.getElementById('nav-to-id').value;
        const shortest = document.getElementById('nav-shortest').value;
        
        try {
            const response = await fetch(`${navigatorApiBaseUrl}/route/${fromId}/${toId}/${shortest}`);
            if (!response.ok) {
                const errorText = await response.text();
                const parsedMsg = parseXmlErrorMessage(errorText);
                const msg = parsedMsg || errorText;
                throw new Error(`Failed to find route: ${msg}`);
            }
            const xmlString = await response.text();
            const route = parseRoutesXml(xmlString)[0];
            showInfo(`Optimal route found: <b>${route.name}</b><br>Distance: ${route.distance}`);
        } catch (error) {
            showError(error.message);
        }
    }

    async function addNavigatorRoute() {
        const fromId = document.getElementById('nav-add-from-id').value;
        const toId = document.getElementById('nav-add-to-id').value;
        const distance = document.getElementById('nav-add-distance').value;

        try {
            const response = await fetch(`${navigatorApiBaseUrl}/route/add/${fromId}/${toId}/${distance}`, { method: 'POST' });
            if (!response.ok) {
                const errorText = await response.text();
                const parsedMsg = parseXmlErrorMessage(errorText);
                const msg = parsedMsg || errorText;
                throw new Error(`Failed to add route: ${msg}`);
            }
            showInfo('Route added successfully!');
            fetchRoutes();
        } catch (error) {
            showError(error.message);
        }
    }

    function populateRouteForm(route) {
        document.getElementById('route-id').value = route.id;
        document.getElementById('route-name').value = route.name;
        showInfo(`Editing route ID: <b>${route.id}</b>. Please fill all fields to update.`);
    }

    function showError(message) {
        showModal({ title: 'Error', message });
    }

    function showInfo(message) {
        showModal({ title: 'Information', message });
    }

    function confirmModal({ title = 'Подтверждение', message = '', onConfirm }) {
        // Build lightweight confirm using existing modal
        modalTitleEl.textContent = title;
        modalBodyEl.innerHTML = message;
        const footer = modalOkBtn.parentElement;
        // Clone primary button to avoid stacked listeners
        const okBtn = modalOkBtn.cloneNode(true);
        okBtn.textContent = 'Confirm';
        okBtn.onclick = () => { hideModal(); if (onConfirm) onConfirm(); };
        const cancelBtn = document.createElement('button');
        cancelBtn.className = 'btn';
        cancelBtn.textContent = 'Cancel';
        cancelBtn.onclick = hideModal;
        footer.innerHTML = '';
        footer.appendChild(cancelBtn);
        footer.appendChild(okBtn);
        modalOverlay.classList.add('show');
        modalOverlay.setAttribute('aria-hidden', 'false');
    }

    fetchRoutes();
});
