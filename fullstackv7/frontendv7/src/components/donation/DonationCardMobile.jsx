// src/components/donation/DonationCardMobile.jsx
import React from 'react';
import {
  Calendar,
  MapPin,
  Droplets,
  FileText,
  Building2,
} from 'lucide-react';
import StatusBadge from '../common/StatusBadge';
import { getDonationStatusConfig } from './DonationStatusConfig';
import { formatDateTime } from '../../utils/formatters';
import { HOSPITAL_INFO } from '../../utils/constants';

const DonationCardMobile = ({ process }) => {
  const statusConfig = getDonationStatusConfig(process.status);
  const StatusIcon = statusConfig.icon;

  return (
    <div className='bg-gray-50 rounded-lg p-4 border border-gray-200'>
      {/* Mobile Header */}
      <div className='flex items-center justify-between mb-3'>
        <div className='flex items-center space-x-3'>
          <div className={`p-2 rounded-lg ${statusConfig.bg}`}>
            <StatusIcon className={`w-5 h-5 ${statusConfig.color}`} />
          </div>
          <div>
            <div className='flex items-center space-x-2'>
              <span className='text-lg font-bold text-red-600'>
                {process.donor?.bloodType || 'N/A'}
              </span>
              <Droplets className='w-4 h-4 text-red-500 fill-current' />
            </div>
          </div>
        </div>
        <StatusBadge status={process.status} />
      </div>

      {/* Note */}
      {process.note && (
        <div className='mb-3'>
          <div className='flex items-start space-x-2'>
            <FileText className='w-4 h-4 text-gray-500 mt-0.5' />
            <p className='text-sm text-gray-700'>{process.note}</p>
          </div>
        </div>
      )}

      {/* Appointment Info */}
      {process.appointment && (
        <div className='mb-3 space-y-2'>
          <div className='flex items-center space-x-2'>
            <Calendar className='w-4 h-4 text-blue-600' />
            <span className='text-sm text-gray-700'>
              {formatDateTime(process.appointment.scheduledDate)}
            </span>
          </div>
          <div className='flex items-center space-x-2'>
            <Building2 className='w-4 h-4 text-green-600' />
            <span className='text-sm text-gray-700'>
              {HOSPITAL_INFO.FULL_NAME}
            </span>
          </div>
          {process.appointment.address && (
            <div className='flex items-center space-x-2'>
              <MapPin className='w-4 h-4 text-gray-500' />
              <span className='text-xs text-gray-500'>
                {process.appointment.address}
              </span>
            </div>
          )}
        </div>
      )}

      {/* Volume & Result */}
      <div className='flex items-center justify-between'>
        <div className='text-xs text-gray-500'>
          ID: {process.id} • {formatDateTime(process.createdAt)}
        </div>
        {process.collectedVolumeMl && (
          <div className='flex items-center text-sm text-green-600'>
            <Droplets className='w-4 h-4 mr-1' />
            <span className='font-semibold'>
              {process.collectedVolumeMl}ml
            </span>
          </div>
        )}
      </div>
    </div>
  );
};

export default DonationCardMobile;
