// src/pages/MyAppointmentsPage.jsx
import React from 'react';
import MainLayout from '../../components/layout/MainLayout';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import Pagination from '../../components/common/Pagination';
import { useMyAppointments } from '../hooks/useMyAppointments';
import {
  AppointmentsPageHeader,
  AppointmentsList,
  RescheduleModal,
} from '../components';

const MyAppointmentsPage = () => {
  const {
    appointments,
    isLoading,
    page,
    totalPages,
    showRescheduleDialog,
    rescheduleReason,
    setPage,
    setRescheduleReason,
    setShowRescheduleDialog,
    handleOpenReschedule,
    handleConfirmReschedule,
    getStatusColor,
  } = useMyAppointments();

  if (isLoading) {
    return (
      <MainLayout>
        <LoadingSpinner message='Đang tải lịch hẹn...' />
      </MainLayout>
    );
  }

  return (
    <div className='container mx-auto px-4 py-8'>
      <AppointmentsPageHeader />
      
      <AppointmentsList
        appointments={appointments}
        getStatusColor={getStatusColor}
        onRequestReschedule={handleOpenReschedule}
      />
      
      {appointments.length > 0 && (
        <div className='mt-8 flex justify-center'>
          <Pagination
            currentPage={page - 1}
            totalPages={totalPages}
            onPageChange={p => setPage(p + 1)}
          />
        </div>
      )}
      
      <RescheduleModal
        isOpen={showRescheduleDialog}
        onClose={() => setShowRescheduleDialog(false)}
        rescheduleReason={rescheduleReason}
        setRescheduleReason={setRescheduleReason}
        onConfirm={handleConfirmReschedule}
      />
    </div>
  );
};

export default MyAppointmentsPage;
