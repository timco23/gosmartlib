import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { Collection, CreateCollectionPayload } from '../../types/types';

@Injectable({
    providedIn: 'root',
})
export class CollectionService {
    private readonly apiBaseUrl = environment.apiUrl;

    constructor(private readonly http: HttpClient) { }

    getCollectionsBySchoolId(id: number): Observable<Collection[]> {
        return this.http.get<Collection[]>(`${this.apiBaseUrl}/collections/school/${id}`);
    }

    getCollectionById(id: number): Observable<Collection> {
        return this.http.get<Collection>(`${this.apiBaseUrl}/collections/${id}`);
    }

    getFeaturedCollectionsBySchoolId(id: number): Observable<Collection[]> {
        return this.http.get<Collection[]>(`${this.apiBaseUrl}/collections/school/${id}/featured`);
    }

    getAllGloballyFeaturedCollections(): Observable<Collection[]> {
        return this.http.get<Collection[]>(`${this.apiBaseUrl}/collections/global/featured`);
    }

    checkIfItemIsInCollection(id: number): Observable<Boolean> {
        return this.http.get<Boolean>(`${this.apiBaseUrl}/collections/exists/${id}`);
    }

    checkIfItemIsInGloballyFeaturedCollection(id: number): Observable<Boolean> {
        return this.http.get<Boolean>(`${this.apiBaseUrl}/collections/global/exists/${id}`);
    }

    addCollection(payload: CreateCollectionPayload): Observable<Collection> {
        return this.http.post<Collection>(`${this.apiBaseUrl}/collections`, payload);
    }

    deleteCollection(id: number): Observable<Collection> {
        return this.http.delete<Collection>(`${this.apiBaseUrl}/collections/${id}`);
    }

    updateCollection(id: number, payload: CreateCollectionPayload): Observable<Collection> {
        return this.http.put<Collection>(`${this.apiBaseUrl}/collections/${id}`, payload);
    }
}