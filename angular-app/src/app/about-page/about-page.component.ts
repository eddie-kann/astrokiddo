import {Component} from '@angular/core';

import {NzCardModule} from 'ng-zorro-antd/card';
import {NzImageModule} from 'ng-zorro-antd/image';
import {NzIconModule} from 'ng-zorro-antd/icon';

@Component({
  selector: 'app-about-page',
  standalone: true,
  imports: [NzCardModule, NzImageModule, NzIconModule],
  templateUrl: './about-page.component.html',
  styleUrls: ['./about-page.component.css']
})
export class AboutPageComponent {
  highlights = [
    {
      title: 'NASA',
      description: 'Powered by the Astronomy Picture of the Day and NASA media APIs.',
      href: 'https://apod.nasa.gov/',
      emoji: '🚀'
    },
    {
      title: 'Florida Institute of Technology',
      description: 'MS Computer Information Systems Final Project',
      href: 'https://www.fit.edu/programs/computer-information-systems-ms/',
      emoji: '🎓'
    }
  ];
}
