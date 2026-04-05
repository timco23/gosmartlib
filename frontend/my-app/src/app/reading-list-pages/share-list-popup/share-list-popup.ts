import { Component, effect, inject, model, Renderer2 } from '@angular/core';

@Component({
  selector: 'app-share-list-popup',
  imports: [],
  templateUrl: './share-list-popup.html',
  styleUrl: './share-list-popup.css',
})
export class ShareListPopup {
  shown = model<boolean>(false);
  link= model<string>("");
  renderer = inject(Renderer2);

  //Locks scrolling for body of the entire page, not just this component.
  constructor() {
    effect(() => {
      if (this.shown()) {
        this.renderer.addClass(document.body, 'no-scroll');
      } else {
        this.renderer.removeClass(document.body, 'no-scroll');
      }
    });
  }

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
