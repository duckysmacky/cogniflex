import { AnalyzeSection } from '@components/analyze-section';
import { Footer } from '@components/footer';
import { Header } from '@components/header';
import { Divider } from '@components/ui';

function App() {
  return (
    <>
      <Header />
      <Divider />
      <AnalyzeSection />
      <Divider />
      <Footer />
    </>
  );
}

export default App;
