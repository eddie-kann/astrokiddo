import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {DeckService, GenerateReq, LessonDeck, PageResponse} from '../deck.service';
import {NzFormModule} from 'ng-zorro-antd/form';
import {NzInputModule} from 'ng-zorro-antd/input';
import {NzSelectModule} from 'ng-zorro-antd/select';
import {NzButtonModule} from 'ng-zorro-antd/button';
import {NzIconModule} from 'ng-zorro-antd/icon';
import {NzSpinModule} from 'ng-zorro-antd/spin';
import {NzListModule} from 'ng-zorro-antd/list';
import {NzCardModule} from 'ng-zorro-antd/card';
import {NzModalModule} from 'ng-zorro-antd/modal';
import {NzTagModule} from 'ng-zorro-antd/tag';
import {NzImageModule} from 'ng-zorro-antd/image';
import {firstValueFrom} from 'rxjs';
import {LoadingService} from '../loading.service';
import {NzImageViewComponent} from 'ng-zorro-antd/experimental/image';

@Component({
  selector: 'app-decks-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    NzFormModule,
    NzInputModule,
    NzSelectModule,
    NzButtonModule,
    NzIconModule,
    NzSpinModule,
    NzListModule,
    NzCardModule,
    NzModalModule,
    NzTagModule,
    NzImageModule,
    NzImageViewComponent
  ],
  templateUrl: './decks-page.component.html',
  styleUrls: ['./decks-page.component.css']
})
export class DecksPageComponent implements OnInit, OnDestroy {
  form = new FormGroup({
    topic: new FormControl('Spiral galaxies', {nonNullable: true, validators: [Validators.required]}),
    gradeLevel: new FormControl('4', {nonNullable: true}),
    locale: new FormControl('en', {nonNullable: true})
  });
  loading = false;
  listLoading = false;
  error?: string;
  decks: LessonDeck[] = [];
  showSlideshow = false;
  selectedDeck?: LessonDeck;
  lastRequest?: GenerateReq;

  @ViewChild('revealRoot') private revealRoot?: ElementRef<HTMLDivElement>;
  private revealInstance?: any;
  private revealInitTimeout?: number;

  constructor(private deckSvc: DeckService, private loadingSvc: LoadingService) {
  }

  ngOnInit() {
    void this.fetchDecks();
  }

  ngOnDestroy() {
    this.destroyReveal();
    if (this.revealInitTimeout) {
      window.clearTimeout(this.revealInitTimeout);
    }
  }

  async fetchDecks() {
    this.listLoading = true;
    this.loadingSvc.show();
    try {
      const page = await firstValueFrom(this.deckSvc.listDecks());
      this.decks = this.extractDecks(page);
    } catch (e) {
      console.warn('Unable to load decks', e);
    } finally {
      this.listLoading = false;
      this.loadingSvc.hide();
    }
  }

  async generateDeck() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.loadingSvc.show();
    this.error = undefined;
    const req = this.form.getRawValue() as GenerateReq;
    this.lastRequest = req;
    try {
      const created = await firstValueFrom(this.deckSvc.generate(req));
      this.decks = [created, ...this.decks];
    } catch (e: any) {
      this.error = e?.message || 'Failed to generate deck';
    } finally {
      this.loading = false;
      this.loadingSvc.hide();
    }
  }

  openSlideshow(deck: LessonDeck) {
    this.selectedDeck = deck;
    this.showSlideshow = true;
    if (this.revealInitTimeout) {
      window.clearTimeout(this.revealInitTimeout);
    }
    this.revealInitTimeout = window.setTimeout(() => this.initializeReveal(), 0);
  }

  closeSlideshow() {
    this.showSlideshow = false;
    if (this.revealInitTimeout) {
      window.clearTimeout(this.revealInitTimeout);
      this.revealInitTimeout = undefined;
    }
    this.destroyReveal();
  }

  trackDeck(_: number, deck: LessonDeck) {
    return deck.id;
  }

  trackSlide(_: number, slide: any) {
    return slide?.title || slide?.text;
  }

  private extractDecks(page: PageResponse<LessonDeck> | LessonDeck[]) {
    if (Array.isArray(page)) {
      return page;
    }
    return page?.content ?? [];
  }

  private initializeReveal() {
    if (!this.selectedDeck) {
      return;
    }
    const container = this.revealRoot?.nativeElement;
    const revealGlobal = (window as any).Reveal;
    if (!revealGlobal || !container) {
      console.warn('Reveal.js failed to load.');
      return;
    }

    this.destroyReveal();

    this.revealInstance = new revealGlobal({
      container,
      embedded: true,
      hash: false,
      controls: true,
      progress: true,
      transition: 'slide',
      backgroundTransition: 'fade'
    });

    if (typeof this.revealInstance.initialize === 'function') {
      this.revealInstance.initialize();
    }
    if (typeof this.revealInstance.sync === 'function') {
      this.revealInstance.sync();
    }
    if (typeof this.revealInstance.layout === 'function') {
      this.revealInstance.layout();
    }
    if (typeof this.revealInstance.slide === 'function') {
      this.revealInstance.slide(0);
    }
  }

  private destroyReveal() {
    if (this.revealInstance && typeof this.revealInstance.destroy === 'function') {
      this.revealInstance.destroy();
    }
    this.revealInstance = undefined;
  }
}
