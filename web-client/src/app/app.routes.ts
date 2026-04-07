import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { AuthCallbackComponent } from './auth/auth-callback.component';
import { HelloComponent } from './hello/hello.component';
import { authGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'auth/callback', component: AuthCallbackComponent },
  { path: 'hello', component: HelloComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
