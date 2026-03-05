export interface Book {
    id: number,
    schoolId: number,
    tags: string,
    description: string,
    featured: boolean,
    itemId: number,
    title: string,
    publisher: string,
    author: string,
    isbn?: string,
    releaseYear: number,
    language: string,
    pageCount: number,
    cover?: string,
    reviewScore: number,
    reviewCount: number
}

export interface UpdateBookPayload {
    tags: string,
    description: string,
    featured: boolean,
    title: string,
    publisher: string,
    author: string,
    releaseYear: number,
    language: string,
    pageCount: number,
    cover: string
}

export interface CreateBookPayload {
    schoolId: number,
    tags: string,
    description: string,
    featured: boolean,
    itemId: number,
    title: string,
    publisher: string,
    author: string,
    isbn?: string,
    releaseYear: number,
    language: string,
    pageCount: number,
    cover?: string,
    reviewScore: number,
    reviewCount: number
}

export interface GlobalBook  { 
    id: number,
    title: string,
    publisher: string,
    author: string,
    isbn?: string,
    releaseYear: number,
    language: string,
    pageCount: number,
    cover?: string,
    reviewScore: number,
    reviewCount: number
}
