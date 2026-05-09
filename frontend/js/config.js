/**
 * Configuración centralizada del frontend
 * Cambiar la URL según el ambiente
 */

const API_CONFIG = {
    // En desarrollo, cambiar a http://localhost:8080
    // En producción, cambiar a la URL del servidor backend
    BASE_URL: 'http://localhost:8080',
    API_PATH: '/api'
};

// URL completa de la API
export const API_BASE_URL = API_CONFIG.BASE_URL + API_CONFIG.API_PATH;

// URLs de endpoints específicos
export const API_ENDPOINTS = {
    AUTH: {
        LOGIN: `${API_BASE_URL}/auth/login`,
        LOGOUT: `${API_BASE_URL}/auth/logout`,
        VERIFY: `${API_BASE_URL}/auth/verify`
    },
    PACIENTES: {
        LIST: `${API_BASE_URL}/pacientes`,
        GET: (id) => `${API_BASE_URL}/pacientes/${id}`,
        CREATE: `${API_BASE_URL}/pacientes`,
        UPDATE: (id) => `${API_BASE_URL}/pacientes/${id}`,
        DELETE: (id) => `${API_BASE_URL}/pacientes/${id}`
    },
    DOCTORES: {
        LIST: `${API_BASE_URL}/doctores`,
        GET: (id) => `${API_BASE_URL}/doctores/${id}`,
        CREATE: `${API_BASE_URL}/doctores`,
        UPDATE: (id) => `${API_BASE_URL}/doctores/${id}`,
        DELETE: (id) => `${API_BASE_URL}/doctores/${id}`
    },
    ESPECIALIDADES: {
        LIST: `${API_BASE_URL}/especialidades`,
        GET: (id) => `${API_BASE_URL}/especialidades/${id}`,
        CREATE: `${API_BASE_URL}/especialidades`,
        UPDATE: (id) => `${API_BASE_URL}/especialidades/${id}`,
        DELETE: (id) => `${API_BASE_URL}/especialidades/${id}`
    },
    CITAS: {
        LIST: `${API_BASE_URL}/citas`,
        GET: (id) => `${API_BASE_URL}/citas/${id}`,
        CREATE: `${API_BASE_URL}/citas`,
        UPDATE: (id) => `${API_BASE_URL}/citas/${id}`,
        DELETE: (id) => `${API_BASE_URL}/citas/${id}`
    },
    PAGOS: {
        LIST: `${API_BASE_URL}/pagos`,
        GET: (id) => `${API_BASE_URL}/pagos/${id}`,
        CREATE: `${API_BASE_URL}/pagos`,
        UPDATE: (id) => `${API_BASE_URL}/pagos/${id}`,
        DELETE: (id) => `${API_BASE_URL}/pagos/${id}`
    }
};
