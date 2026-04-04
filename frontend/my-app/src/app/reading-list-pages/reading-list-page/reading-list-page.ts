import { Component, OnInit, inject } from '@angular/core';
import { ReadingListService } from '../../services/readingList-service';
import { Book, ReadingList, UserProfile } from '../../../types/types';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { Router } from '@angular/router';
import { ShareListPopup } from '../share-list-popup/share-list-popup';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-reading-list-page',
  imports: [ShareListPopup],
  templateUrl: './reading-list-page.html',
  styleUrl: './reading-list-page.css',
})
export class ReadingListPage implements OnInit {
  user: UserProfile | null = null;
  readingLists: (ReadingList & { scrollIndex: number })[] | null = null
  loading: boolean = true;
  loadingError: boolean = false;
  showPopup: boolean = false;
  copyLink: string = "";
  readonly baseUrl = environment.url;

  readingListService = inject(ReadingListService);
  userService = inject(UserService);
  authService = inject(AuthService);
  router = inject(Router);

  ngOnInit(): void {
    this.authService.currentUser$.subscribe({
      next: (user) => {
        if (user) {
          this.userService.getUser(user.email).subscribe({
            next: (userData) => {
              this.user = userData;
              this.getLists();
            },
            error: () => { this.loadingError = true }
          })
        }
      },
      error: () => { this.loadingError = true }
    });
  }

  deleteList(id: number) {
    this.readingListService.deleteReadingList(id).subscribe({
      next: () => this.getLists()
    });
  }

  getLists() {
    this.loading = true;
    this.readingListService.getReadingListsByUserid(this.user!.id).subscribe({
      next: (lists) => {
        this.readingLists = lists.map(list => ({ ...list, scrollIndex: 0 }));
        this.loading = false;
      },
      error: () => { this.loadingError = true }
    })
  }

  editList(id: number) {
    this.router.navigate(['/lists', id, '/edit'])
  }

  shareList(id: number) {
    const link = `${this.baseUrl}/lists/${id}`
    this.copyLink = link;
    this.showPopup = true;
  }

  addList() {
    this.router.navigate(['/lists/add'])  
  }

  goToBookDetails(id: number) {
    this.router.navigate(['/books', id])
  }

  getVisibleBooks(list: ReadingList & { scrollIndex: number }): Book[] {
    const books = list.items || [];
    if (books.length <= 5) return books;

    const visible: Book[] = [];
    for (let i = 0; i < 5; i++) {
      const index = (list.scrollIndex + i) % books.length;
      visible.push(books[index]);
    }
    return visible;
  }

  nextBooks(list: ReadingList & { scrollIndex: number }) {
    if (list.items.length > 0) {
      list.scrollIndex = (list.scrollIndex + 1) % list.items.length;
    }
  }

  prevBooks(list: ReadingList & { scrollIndex: number }) {
    if (list.items.length > 0) {
      const length = list.items.length;
      list.scrollIndex = (list.scrollIndex - 1 + length) % length;
    }
  }
}
