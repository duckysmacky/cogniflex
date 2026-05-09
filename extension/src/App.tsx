import { AnalyzeSection, SettingsSection } from '@/sections';
import { Footer } from '@components/footer';
import { Header } from '@components/header';
import { Divider } from '@components/ui';
import { HashRouter, Route, Routes } from 'react-router-dom';

function App() {
  return (
    <HashRouter>
      <Header />
      <Divider />
      <Routes>
        <Route path="/" element={<AnalyzeSection />} />
        <Route path="/settings" element={<SettingsSection />} />
      </Routes>
      <Divider />
      <Footer />
    </HashRouter>
  );
}

export default App;
