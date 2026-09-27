import { Component, OnInit, inject, signal } from '@angular/core';

import { AppointmentCard } from '../appointment-card/appointment-card';
import { Appointment, Appointments } from '../services/appointments';

@Component({
  selector: 'app-home',
  imports: [AppointmentCard],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home implements OnInit {
  private readonly appointmentsService = inject(Appointments);

  protected readonly appointments = signal<Appointment[]>([]);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');

  ngOnInit(): void {
    if (localStorage.getItem('role') === 'ADMIN') {
      this.loading.set(false);
      return;
    }

    this.appointmentsService.getMine().subscribe({
      next: (appointments) => {
        this.appointments.set(appointments);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('No se pudieron cargar tus citas');
        this.loading.set(false);
      },
    });
  }
}
