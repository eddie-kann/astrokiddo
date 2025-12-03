import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DeckService, ApodResponse} from '../deck.service';
import {NzCardModule} from 'ng-zorro-antd/card';
import {NzDatePickerModule} from 'ng-zorro-antd/date-picker';
import {NzImageModule} from 'ng-zorro-antd/image';
import {NzButtonModule} from 'ng-zorro-antd/button';
import {NzIconModule} from 'ng-zorro-antd/icon';
import {NzSpinModule} from 'ng-zorro-antd/spin';
import {FormsModule} from '@angular/forms';
import {firstValueFrom} from 'rxjs';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [CommonModule, FormsModule, NzCardModule, NzDatePickerModule, NzImageModule, NzButtonModule, NzIconModule, NzSpinModule],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css'
})
export class HomePageComponent implements OnInit, OnDestroy {
  apod?: ApodResponse;
  loading = false;
  error?: string;
  selectedDate: Date | null = null;
  audio?: HTMLAudioElement;
  playing = false;

  constructor(private deckSvc: DeckService) {
  }

  ngOnInit() {
    this.loadApod();
  }

  ngOnDestroy() {
    this.stopAudio();
  }

  async loadApod(date?: Date | null) {
    this.loading = true;
    this.error = undefined;
    this.apod = undefined;
    const formattedDate = date ? this.formatDate(date) : undefined;
    try {
      this.apod = await firstValueFrom(this.deckSvc.apod(formattedDate));
      this.setupAudio();
    } catch (e: any) {
      this.error = e?.message || 'Unable to load Astronomy Picture of the Day';
    } finally {
      this.loading = false;
    }
  }

  onDateChange(date: Date | null) {
    this.selectedDate = date;
    this.stopAudio();
    this.loadApod(date);
  }

  toggleAudio() {
    if (!this.audio) {
      return;
    }
    if (this.playing) {
      this.audio.pause();
      this.playing = false;
    } else {
      this.audio.play();
      this.playing = true;
    }
  }

  private setupAudio() {
    this.stopAudio();
    if (this.apod?.ttsAudioUrl) {
      this.audio = new Audio(this.apod.ttsAudioUrl);
      this.audio.onended = () => this.playing = false;
    }
  }

  private stopAudio() {
    if (this.audio) {
      this.audio.pause();
    }
    this.playing = false;
  }

  private formatDate(date: Date): string {
    const iso = date.toISOString();
    return iso.split('T')[0];
  }

  get mediaIsImage(): boolean {
    return this.apod?.media_type === 'image';
  }
}
