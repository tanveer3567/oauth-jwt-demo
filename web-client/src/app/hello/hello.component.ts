import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HelloService, Prompt } from './hello.service';
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
      } @else {
        <h1 style="font-size:28px;margin:0 0 8px;">Prompts</h1>

        <hr style="border:none;border-top:1px solid #eee;margin-bottom:24px;">

        @for (prompt of prompts; track prompt.id) {
          <div style="margin-bottom:16px;padding:16px;border:1px solid #eee;border-radius:6px;">
            <strong>{{ prompt.title }}</strong>
            <span style="margin-left:8px;font-size:12px;color:#888;background:#f5f5f5;padding:2px 6px;border-radius:4px;">{{ prompt.category }}</span>
            <p style="margin:6px 0 0;color:#555;font-size:14px;">{{ prompt.description }}</p>
          </div>
        }
      }

    </div>
  `
})
export class HelloComponent implements OnInit {
  prompts: Prompt[] = [];
  loading = true;
  error = '';

  constructor(
    private helloService: HelloService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.helloService.getPrompts().subscribe({
      next: (res) => { this.prompts = res; this.loading = false; },
      error: () => { this.error = 'Failed to load prompts.'; this.loading = false; }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
