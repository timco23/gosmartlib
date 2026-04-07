import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../services/book-service';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { SchoolService } from '../../services/school.service';
import { SchoolItemCopy } from '../../../types/types';

interface BarcodePrintItem {
  copy: SchoolItemCopy;
  selected: boolean;
  imageUrl: string | null;
}

@Component({
  selector: 'app-barcode-print-page',
  imports: [FormsModule],
  templateUrl: './barcode-print-page.html',
  styleUrl: './barcode-print-page.css',
})
export class BarcodePrintPage implements OnInit, OnDestroy {
  private api = inject(BookService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private schoolService = inject(SchoolService);

  items: BarcodePrintItem[] = [];
  loading = true;

  private objectUrls: string[] = [];

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const bookId = parseInt(params.get('id')!);
      this.loadCopies(bookId);
    });
  }

  private loadCopies(bookId: number): void {
    this.authService.currentUser$.subscribe((user) => {
      if (!user?.email) return;
      this.userService.getUser(user.email).subscribe({
        next: (userData) => {
          if (!userData.schoolName) return;
          this.schoolService.getByName(userData.schoolName).subscribe({
            next: (school) => {
              this.api.loadCopies(bookId, school.id!).subscribe({
                next: (copies) => {
                  this.items = copies.map((copy) => ({ copy, selected: false, imageUrl: null }));
                  this.loading = false;
                  this.fetchBarcodeImages();
                },
              });
            },
          });
        },
      });
    });
  }

  private fetchBarcodeImages(): void {
    this.items.forEach((item) => {
      this.api.getBarcodePng(item.copy.id).subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(blob);
          this.objectUrls.push(url);
          item.imageUrl = url;
        },
      });
    });
  }

  toggleAll(selected: boolean): void {
    this.items.forEach((item) => (item.selected = selected));
  }

  get selectedItems(): BarcodePrintItem[] {
    return this.items.filter((item) => item.selected);
  }

  print(): void {
    window.print();
  }

  back(): void {
    this.router.navigate(['../..'], { relativeTo: this.route });
  }

  ngOnDestroy(): void {
    this.objectUrls.forEach((url) => URL.revokeObjectURL(url));
  }
}
