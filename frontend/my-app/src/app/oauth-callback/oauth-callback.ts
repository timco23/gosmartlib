import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-oauth-callback',
  standalone: true,
  templateUrl: './oauth-callback.html',
  styleUrl: './oauth-callback.css',
})

//Component after Smartschool redirect
export class OauthCallback implements OnInit {
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private auth: AuthService,
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    const refreshToken = this.route.snapshot.queryParamMap.get('refreshToken');

    if (!token || !refreshToken) {
      this.router.navigate(['/login'], { queryParams: { oauth: 'missing_token' } });
      return;
    }

    this.auth.setToken(token);
    this.auth.setRefreshToken(refreshToken);

    this.auth.me().subscribe({
      next: () => this.router.navigate(['/home']),
      error: () => {
        this.auth.logout();
        this.router.navigate(['/login'], { queryParams: { oauth: 'me_failed' } });
      },
    });
  }
}
