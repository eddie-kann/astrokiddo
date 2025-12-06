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
import {NzTagModule} from 'ng-zorro-antd/tag';
import {firstValueFrom} from 'rxjs';
import {LoadingService} from '../loading.service';
import Reveal from 'reveal.js';

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
    NzTagModule
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
  slideshowLoading = false;
  slideZooming = false;
  slideZoomOrigin = '50% 50%';

  @ViewChild('revealRoot') private revealRoot?: ElementRef<HTMLDivElement>;
  private revealInstance?: Reveal.Api;

  constructor(private deckSvc: DeckService, private loadingSvc: LoadingService) {
  }

  ngOnInit() {
    void this.fetchDecks();
  }

  ngOnDestroy() {
    this.destroyReveal();
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

  async openSlideshow(deck: LessonDeck) {
    this.selectedDeck = deck;
    this.showSlideshow = true;
    this.slideshowLoading = true;
    try {
      this.selectedDeck = await firstValueFrom(this.deckSvc.getDeckById(deck.id));
    } catch (e) {
      console.warn('Unable to load deck details', e);
    } finally {
      this.slideshowLoading = false;
      setTimeout(() => void this.initializeReveal());
    }
  }

  closeSlideshow() {
    this.showSlideshow = false;
    this.slideshowLoading = false;
    this.selectedDeck = undefined;
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

  private async initializeReveal() {
    if (!this.selectedDeck || !this.revealRoot?.nativeElement) {
      return;
    }

    this.destroyReveal();

    const revealContainer = this.revealRoot.nativeElement;
    this.revealInstance = new Reveal(revealContainer, {
      embedded: true,
      hash: false,
      controls: true,
      progress: true,
      transition: 'slide',
      backgroundTransition: 'fade',
      width: 1280,
      height: 720,
      margin: 0.06,
      minScale: 0.35,
      maxScale: 1
    });

    await this.revealInstance.initialize();
    this.revealInstance.on('slidechanged', () => this.resetSlideZoomOrigin());
    this.revealInstance.layout();
    this.revealInstance.slide(0);
  }

  onSlideZoomEnter(event: MouseEvent) {
    this.slideZooming = true;
    this.updateSlideZoomOrigin(event);
  }

  onSlideZoomMove(event: MouseEvent) {
    if (!this.slideZooming) {
      return;
    }
    this.updateSlideZoomOrigin(event);
  }

  onSlideZoomLeave() {
    this.slideZooming = false;
    this.resetSlideZoomOrigin();
  }

  private updateSlideZoomOrigin(event: MouseEvent) {
    const target = event.currentTarget as HTMLElement | null;
    if (!target) {
      return;
    }
    const rect = target.getBoundingClientRect();
    const x = ((event.clientX - rect.left) / rect.width) * 100;
    const y = ((event.clientY - rect.top) / rect.height) * 100;
    this.slideZoomOrigin = `${x.toFixed(2)}% ${y.toFixed(2)}%`;
  }

  private resetSlideZoomOrigin() {
    this.slideZoomOrigin = '50% 50%';
  }

  private destroyReveal() {
    this.revealInstance?.destroy();
    this.revealInstance = undefined;
  }
}
