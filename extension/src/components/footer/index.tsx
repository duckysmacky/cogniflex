import { useT } from '@/i18n';

export const Footer = () => {
  const t = useT();

  return (
    <div className="flex justify-between px-5 py-3">
      <a href="" className="text-2xs text-gray">
        {t.footer.about}
      </a>
      <a
        href="https://github.com/duckysmacky/cogniflex"
        target="_blank"
        className="text-2xs text-gray"
      >
        {t.footer.reportBug}
      </a>
    </div>
  );
};
