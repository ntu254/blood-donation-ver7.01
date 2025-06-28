// src/services/appointmentService.js
import apiClient from './apiClient';

export const appointmentService = {
  // Create appointment (Admin/Staff only)
  createAppointment: async appointmentData => {
    try {
      const response = await apiClient.post('/appointments', appointmentData);
      return response.data;
    } catch (error) {
      console.error('=== APPOINTMENT SERVICE ERROR (createAppointment) ===');
      console.error(error.response?.data);
      throw error;
    }
  },

  // Request reschedule (Admin/Staff only)
  requestReschedule: async (appointmentId, rescheduleData) => {
    try {
      const response = await apiClient.put(
        `/appointments/${appointmentId}/request-reschedule`,
        rescheduleData
      );
      return response.data;
    } catch (error) {
      console.error('=== APPOINTMENT SERVICE ERROR (requestReschedule) ===');
      console.error(error.response?.data);
      throw error;
    }
  },

  // Bổ sung: Lấy appointments của donor
  getMyAppointments: async () => {
    try {
      const response = await apiClient.get('/appointments/my-appointments');
      return response.data;
    } catch (error) {
      console.error('=== APPOINTMENT SERVICE ERROR (getMyAppointments) ===');
      console.error(error.response?.data);
      throw error;
    }
  },

  // Note: No getAllAppointments endpoint exists in backend
  // Staff/Admin should manage appointments through donation process management
};

export default appointmentService;
