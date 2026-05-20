import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { environment } from '../../environments/environment';

export interface Prompt {
  id: string;
  title: string;
  description: string;
  category: string;
}

@Injectable({
  providedIn: 'root'
})
export class HelloService {
  constructor(private http: HttpClient, private authService: AuthService) {}

  getPrompts(): Observable<Prompt[]> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.get<Prompt[]>(`${environment.apiUrl}/api/v1/prompts`, { headers });
  }
}
