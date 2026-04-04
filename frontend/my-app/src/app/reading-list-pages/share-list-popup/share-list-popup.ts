import { Component, model } from '@angular/core';

@Component({
  selector: 'app-share-list-popup',
  imports: [],
  templateUrl: './share-list-popup.html',
  styleUrl: './share-list-popup.css',
})
export class ShareListPopup {
  shown = model<boolean>(false);
  link= model<string>("");

  closePopup() {
    this.shown.update(() => false);
  }

  copyInputMessage(){
    const inputElement:HTMLInputElement = document.querySelector("input")!;
    inputElement.select();
    document.execCommand('copy');
    inputElement.setSelectionRange(0, 0);
  }
}
