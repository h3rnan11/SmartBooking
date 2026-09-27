import { Component, input } from '@angular/core';

import { Appointment } from '../services/appointments';

@Component({
  selector: 'app-appointment-card',
  imports: [],
  templateUrl: './appointment-card.html',
  styleUrl: './appointment-card.scss',
})
export class AppointmentCard {
  appointment = input.required<Appointment>();
}
