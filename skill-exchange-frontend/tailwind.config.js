/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Plus Jakarta Sans', 'ui-sans-serif', 'system-ui'],
        display: ['Fraunces', 'serif']
      },
      boxShadow: {
        glow: '0 20px 70px rgba(124, 58, 237, 0.18)'
      }
    }
  },
  plugins: []
};
