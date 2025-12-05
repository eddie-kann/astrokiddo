import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
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
import {NzImageViewComponent} from 'ng-zorro-antd/experimental/image';
import WaveSurfer from '../vendor/wavesurfer.js';
import {LoadingService} from '../loading.service';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [CommonModule, FormsModule, NzCardModule, NzDatePickerModule, NzImageModule, NzButtonModule, NzIconModule, NzSpinModule, NzImageViewComponent, RouterLink],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css'
})
export class HomePageComponent implements OnInit, OnDestroy {
  apod?: ApodResponse;
  loading = false;
  error?: string;
  selectedDate: Date | null = null;
  playing = false;
  private waveSurfer?: WaveSurfer;
  @ViewChild('waveformContainer') waveformContainer?: ElementRef<HTMLDivElement>;

  constructor(private deckSvc: DeckService, private loadingSvc: LoadingService) {
  }

  ngOnInit() {
    void this.loadApod();
  }

  ngOnDestroy() {
    this.stopAudio();
  }

  async loadApod(date?: Date | null) {
    this.loading = true;
    this.error = undefined;
    this.apod = undefined;
    this.loadingSvc.show();
    const formattedDate = date ? this.formatDate(date) : undefined;
    try {
      this.apod = await firstValueFrom(this.deckSvc.apod(formattedDate));
      this.setupAudio();
    } catch (e: any) {
      this.error = e?.message || 'Unable to load Astronomy Picture of the Day';
    } finally {
      this.loading = false;
      this.loadingSvc.hide();
    }
  }

  onDateChange(date: Date | null) {
    this.selectedDate = date;
    this.stopAudio();
    void this.loadApod(date);
  }

  toggleAudio() {
    if (!this.waveSurfer) return;
    this.waveSurfer.playPause();
    this.playing = !!this.waveSurfer.isPlaying?.();
  }

  private setupAudio() {
    this.stopAudio();
    if (this.apod?.tts_audio_url) {
      window.setTimeout(() => this.initializeWaveform(), 0);
    }
  }

  private stopAudio() {
    if (this.waveSurfer) {
      this.waveSurfer.destroy();
      this.waveSurfer = undefined;
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

  get apodImageSrc(): string {
    return this.apod?.hdurl || this.apod?.url || '';
  }

  private initializeWaveform() {
    if (!this.apod?.tts_audio_url || !this.waveformContainer) {
      return;
    }
    this.waveSurfer?.destroy();
    this.waveSurfer = WaveSurfer.create({
      container: this.waveformContainer.nativeElement,
      waveColor: '#6aa9ff',
      progressColor: '#ffffff'
    });
    this.waveSurfer.load(this.apod.tts_audio_url);
    this.waveSurfer.on('play', () => this.playing = true);
    this.waveSurfer.on('pause', () => this.playing = false);
    this.waveSurfer.on('finish', () => this.playing = false);
  }
}
