import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Class } from '../../types/types';
import { ClassService } from '../services/class.service';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-class-list-page',
  imports: [RouterLink, CommonModule],
  templateUrl: './class-list-page.html',
  styleUrl: './class-list-page.css',
})
export class ClassListPage implements OnInit {
  private classApi = inject(ClassService);
  private userApi = inject(UserService);
  private cdr = inject(ChangeDetectorRef);

  classes: Class[] = [];
  loading = true;
  error = false;

  ngOnInit(): void {
    this.userApi.getUser().subscribe((user) => {
      if (user.schoolId) {
        this.classApi.getClassList(user.schoolId).subscribe({
          next: (data) => {
            this.classes = data;
            this.loading = false;
            this.cdr.detectChanges();
          },
          error: () => {
            this.error = true;
            this.loading = false;
            this.cdr.detectChanges();
          },
        });
      }
    });
  }
}
