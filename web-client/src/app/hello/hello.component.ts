import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HelloService } from './hello.service';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-hello',
  standalone: true,
  template: `
    <div style="max-width:600px;margin:80px auto;padding:0 24px;font-family:sans-serif;">

      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:48px;">
        <span style="font-size:18px;font-weight:600;">MyApp</span>
        <button (click)="logout()"
          style="padding:8px 18px;font-size:14px;cursor:pointer;border:1px solid #ccc;background:#fff;border-radius:4px;">
          Logout
        </button>
      </div>

      @if (loading) {
        <p style="color:#888;">Loading...</p>
      } @else if (error) {
        <p style="color:#c00;">{{ error }}</p>
      } @else if (user) {
        <h1 style="font-size:28px;margin:0 0 8px;">{{ user.message }}</h1>
        <p style="color:#555;margin:0 0 40px;">{{ user.email }}</p>

        <hr style="border:none;border-top:1px solid #eee;margin-bottom:40px;">

        <p style="color:#888;font-size:14px;">You are successfully authenticated via Google OAuth2.</p>
      }

    </div>
  `
})
export class HelloComponent implements OnInit {
  user: { message: string; name: string; email: string } | null = null;
  loading = true;
  error = '';

  constructor(
    private helloService: HelloService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.helloService.getHello().subscribe({
      next: (res) => { this.user = res; this.loading = false; },
      error: () => { this.error = 'Failed to load user info.'; this.loading = false; }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
