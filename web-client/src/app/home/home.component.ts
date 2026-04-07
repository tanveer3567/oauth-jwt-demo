import { Component } from '@angular/core';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  template: `
    <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;min-height:100vh;gap:16px;">
      <h1>Welcome</h1>
      <div style="display:flex;gap:12px;">
        <button (click)="authService.signup()" style="padding:10px 24px;font-size:16px;cursor:pointer;">Sign Up</button>
        <button (click)="authService.login()" style="padding:10px 24px;font-size:16px;cursor:pointer;">Log In</button>
      </div>
    </div>
  `
})
export class HomeComponent {
  constructor(public authService: AuthService) {}
}
