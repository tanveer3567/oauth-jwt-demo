import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HelloService {
  constructor(private http: HttpClient, private authService: AuthService) {}

  getHello(): Observable<{ message: string }> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.get<{ message: string }>(`${environment.apiUrl}/api/hello`, { headers });
  }
}
