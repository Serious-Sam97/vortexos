import { createGlobalStyle, ThemeProvider } from 'styled-components';
import { styleReset } from 'react95';
import original from 'react95/dist/themes/original';
import ms_sans_serif from 'react95/dist/fonts/ms_sans_serif.woff2';
import ms_sans_serif_bold from 'react95/dist/fonts/ms_sans_serif_bold.woff2';
import { BrowserRouter, Routes, Route } from "react-router";
import Intro from './pages/Intro';
import Windows from './pages/Windows';

const GlobalStyles = createGlobalStyle`
  ${styleReset}
  
  @font-face {
    font-family: 'ms_sans_serif';
    src: url('${ms_sans_serif}') format('woff2');
    font-weight: 400;
    font-style: normal;
  }

  @font-face {
    font-family: 'ms_sans_serif';
    src: url('${ms_sans_serif_bold}') format('woff2');
    font-weight: bold;
    font-style: normal;
  }

  html, body {
    width: 100%;
    height: 100%;
    margin: 0;
    padding: 0;
  }

  body {
    font-family: 'ms_sans_serif';
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #008080; /* Background for full screen */
  }
`;





const App = () => (
  <BrowserRouter>
      <GlobalStyles />
      <ThemeProvider theme={original}>
        <Routes>
          <Route path="/" element={<Intro />} />
          <Route path="/windows" element={<Windows />} />
        </Routes>
      </ThemeProvider>
    </BrowserRouter>
);

export default App;