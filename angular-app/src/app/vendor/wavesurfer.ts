export type WaveSurferOptions = {
  container: string | HTMLElement;
  waveColor?: string;
  progressColor?: string;
};

export type WaveSurferEvent = 'play' | 'pause' | 'finish';

export default class WaveSurfer {
  static create(options: WaveSurferOptions) {
    return new WaveSurfer(options);
  }

  private container!: HTMLElement;
  private canvas!: HTMLCanvasElement;
  private ctx: CanvasRenderingContext2D | null = null;
  private audio?: HTMLAudioElement;
  private audioCtx?: AudioContext;
  private analyser?: AnalyserNode;
  private dataArray?: Uint8Array;
  private rafId?: number;
  private readonly waveColor: string;
  private readonly progressColor: string;
  private events = new Map<WaveSurferEvent, Set<() => void>>();

  private constructor(options: WaveSurferOptions) {
    this.waveColor = options.waveColor || '#6aa9ff';
    this.progressColor = options.progressColor || '#ffffff';
    this.resolveContainer(options.container);
    this.setupCanvas();
  }

  load(url: string) {
    this.destroyAudio();
    this.audio = new Audio(url);
    this.audio.crossOrigin = 'anonymous';
    this.audio.addEventListener('ended', () => this.emit('finish'));
    this.audio.addEventListener('pause', () => this.emit('pause'));
    this.audio.addEventListener('play', () => this.emit('play'));
    this.createAnalyser();
  }

  playPause() {
    if (!this.audio) return;
    if (this.audio.paused) {
      this.audioCtx?.resume();
      void this.audio.play();
    } else {
      this.audio.pause();
    }
  }

  isPlaying() {
    return !!this.audio && !this.audio.paused;
  }

  on(event: WaveSurferEvent, handler: () => void) {
    if (!this.events.has(event)) {
      this.events.set(event, new Set());
    }
    this.events.get(event)?.add(handler);
  }

  destroy() {
    this.destroyAudio();
    this.events.clear();
    if (this.rafId) {
      cancelAnimationFrame(this.rafId);
    }
    if (this.canvas?.parentElement === this.container) {
      this.container.removeChild(this.canvas);
    }
  }

  private destroyAudio() {
    if (this.audio) {
      this.audio.pause();
    }
    if (this.audioCtx) {
      this.audioCtx.close().catch(() => undefined);
    }
    this.audio = undefined;
    this.audioCtx = undefined;
    this.analyser = undefined;
    this.dataArray = undefined;
    if (this.rafId) {
      cancelAnimationFrame(this.rafId);
      this.rafId = undefined;
    }
  }

  private resolveContainer(container: string | HTMLElement) {
    if (typeof container === 'string') {
      const element = document.querySelector<HTMLElement>(container);
      if (!element) {
        throw new Error('WaveSurfer container not found');
      }
      this.container = element;
    } else {
      this.container = container;
    }
  }

  private setupCanvas() {
    this.canvas = document.createElement('canvas');
    this.canvas.style.width = '100%';
    this.canvas.style.height = '60px';
    this.ctx = this.canvas.getContext('2d');
    this.container.innerHTML = '';
    this.container.appendChild(this.canvas);
    this.resizeCanvas();
  }

  private createAnalyser() {
    if (!this.audio) return;
    this.audioCtx = new AudioContext();
    const source = this.audioCtx.createMediaElementSource(this.audio);
    this.analyser = this.audioCtx.createAnalyser();
    this.analyser.fftSize = 512;
    source.connect(this.analyser);
    this.analyser.connect(this.audioCtx.destination);
    this.dataArray = new Uint8Array(this.analyser.frequencyBinCount);
    this.draw();
  }

  private draw = () => {
    if (!this.ctx || !this.analyser || !this.dataArray) return;
    this.resizeCanvas();
    this.analyser.getByteTimeDomainData(this.dataArray as Uint8Array<ArrayBuffer>);

    const {width, height} = this.canvas;
    this.ctx.clearRect(0, 0, width, height);

    const gradient = this.ctx.createLinearGradient(0, 0, width, height);
    gradient.addColorStop(0, this.waveColor);
    gradient.addColorStop(1, this.progressColor);
    this.ctx.fillStyle = 'rgba(10, 16, 32, 0.35)';
    this.ctx.fillRect(0, 0, width, height);

    this.ctx.lineWidth = 2;
    this.ctx.strokeStyle = gradient;
    this.ctx.beginPath();

    const sliceWidth = width / this.dataArray.length;
    let x = 0;
    for (let i = 0; i < this.dataArray.length; i++) {
      const v = this.dataArray[i] / 128.0;
      const y = (v * height) / 2;
      if (i === 0) {
        this.ctx.moveTo(x, y);
      } else {
        this.ctx.lineTo(x, y);
      }
      x += sliceWidth;
    }

    this.ctx.lineTo(width, height / 2);
    this.ctx.stroke();
    this.rafId = requestAnimationFrame(this.draw);
  };

  private resizeCanvas() {
    if (!this.canvas) return;
    const rect = this.container.getBoundingClientRect();
    this.canvas.width = rect.width * devicePixelRatio;
    this.canvas.height = 60 * devicePixelRatio;
    if (this.ctx) {
      this.ctx.scale(devicePixelRatio, devicePixelRatio);
    }
  }

  private emit(event: WaveSurferEvent) {
    this.events.get(event)?.forEach(handler => handler());
  }
}
