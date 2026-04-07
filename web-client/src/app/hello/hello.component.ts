import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HelloService } from './hello.service';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-hello',
  standalone: true,
  template: `
    <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;min-height:100vh;gap:16px;">
      <h1>{{ message }}</h1>
      <button (click)="logout()" style="padding:10px 24px;font-size:16px;cursor:pointer;">Logout</button>
    </div>
  `
})
export class HelloComponent implements OnInit {
  message = 'Loading...';

  constructor(
    private helloService: HelloService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.helloService.getHello().subscribe({
      next: (res) => { this.message = res.message; },
      error: () => { this.message = 'Error loading greeting'; }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
