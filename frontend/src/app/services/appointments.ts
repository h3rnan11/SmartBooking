import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

const API_URL = 'http://localhost:8080/smartBooking/smart-booking/appointments';

export interface Appointment {
  id: number;
  date: string;
  startTime: string;
  Name: string;
  Location: string;
}

@Injectable({
  providedIn: 'root',
})
export class Appointments {
  private readonly http = inject(HttpClient);

  getMine(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${API_URL}/me`);
  }
}
