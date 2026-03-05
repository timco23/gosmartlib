import { Component, OnInit, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Book } from '../models/book-model';
import { ActivatedRoute, Router } from '@angular/router';
import { BookService } from '../services/book-service';

@Component({
  selector: 'app-book-detail-page',
  imports: [],
  templateUrl: './book-detail-page.html',
  styleUrl: './book-detail-page.css',
})
export class BookDetailPage implements OnInit {
  book: Book | null = null;
  api = inject(BookService);
  router = inject(Router);
  route = inject(ActivatedRoute);


  ngOnInit(): void {
    this.route.paramMap.subscribe(p => {
      const id = p.get("id")
        if (id) {
          this.getBook(id);
        }
    })
  }

  getBook(id: string) {
    const data = this.api.getBookById(parseInt(id));
    data.subscribe({
      next: (data) => (this.book= data)
    })
  }

  deleteBook(id: number) {
    this.api.deleteBook(id).subscribe();
    this.backToList();
  }

  backToList() {
    this.router.navigate(["/books"]);
  }

}
