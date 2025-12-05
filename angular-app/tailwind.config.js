/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        'space-void': '#020617',
        'space-surface': '#0b1020',
        'space-mid': '#0f172a',
        'cosmic-violet': '#7c3aed',
        'cosmic-cyan': '#22d3ee',
        'cosmic-amber': '#fbbf24',
        'cosmic-white': '#e5e7eb',
      },
      backgroundImage: {
        'cosmic-gradient': 'radial-gradient(circle at 20% 20%, rgba(124, 58, 237, 0.16), transparent 32%), radial-gradient(circle at 80% 8%, rgba(34, 211, 238, 0.12), transparent 30%), radial-gradient(circle at 50% 80%, rgba(251, 191, 36, 0.08), transparent 38%), linear-gradient(145deg, #0b1020, #05060a 55%, #0a0f1d 90%)',
        'cosmic-aurora': 'radial-gradient(circle at 10% 20%, rgba(124, 58, 237, 0.24), transparent 28%), radial-gradient(circle at 80% 20%, rgba(34, 211, 238, 0.22), transparent 30%)',
      },
      boxShadow: {
        'cosmic': '0 10px 35px rgba(124, 58, 237, 0.22)',
        'cosmic-strong': '0 20px 50px rgba(34, 211, 238, 0.26)',
      },
      animation: {
        'float-slow': 'float 12s ease-in-out infinite',
        'twinkle': 'star-twinkle 14s linear infinite',
      },
    },
  },
  plugins: [],
}

