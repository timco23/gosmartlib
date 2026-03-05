import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BookService } from '../../services/book-service';
import { Book } from '../../../types/types';

@Component({
  selector: 'app-book-detail-page',
  imports: [RouterLink],
  templateUrl: './book-detail-page.html',
  styleUrl: './book-detail-page.css',
})
export class BookDetailPage implements OnInit {
  book: Book | null = null;
  api = inject(BookService);
  router = inject(Router);
  route = inject(ActivatedRoute);
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);

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
      next: (data) => {
        this.book = data;
        this.cdr.detectChanges();
      }
    })
  }

  deleteBook(id: number) {
    this.api.deleteBook(id).subscribe({next: () => this.backToList()});
  }

  backToList() {
    this.router.navigate(["/books"]);
  }

}
