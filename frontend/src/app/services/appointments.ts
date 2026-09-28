import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';

const API_URL = `${environment.apiUrl}/appointments`;

export interface Appointment {
  id: number;
  date: string;
  startTime: string;
  name: string;
  location: string;
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
