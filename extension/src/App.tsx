import { useLanguage } from '@/i18n';
import { AnalyzeSection, SettingsSection } from '@/sections';
import { Footer } from '@components/footer';
import { Header } from '@components/header';
import { Divider } from '@components/ui';
import { useEffect } from 'react';
import { HashRouter, Route, Routes } from 'react-router-dom';

function App() {
  const language = useLanguage();

  useEffect(() => {
    document.documentElement.lang = language;
  }, [language]);

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
