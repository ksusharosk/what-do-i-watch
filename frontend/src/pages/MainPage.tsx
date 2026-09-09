import { useTranslation } from "react-i18next"
import { useQuery } from "@tanstack/react-query"

export default function MainPage() {
    const { t, i18n } = useTranslation()

    const { data, isLoading, error } = useQuery({
        queryKey: ['health'],
        queryFn: async () => {
            const res = await fetch('http://localhost:8080/health')
            if (!res.ok) throw new Error('Health check failed')
            return res.text()
        },
    })

    return (
        <div className="p-8 space-y-4">
            <h1 className="text-2xl">{t('greeting')}</h1>
            <div className="space-x-2">
                <button className="underline" onClick={() => i18n.changeLanguage('en')}>EN</button>
                <button className="underline" onClick={() => i18n.changeLanguage('ru')}>RU</button>
                <button className="underline" onClick={() => i18n.changeLanguage('pl')}>PL</button>
            </div>
            <div className="text-sm text-gray-500">
                Backend says: {isLoading ? 'checking...' : error ? `ERROR: ${error.message}` : data }
            </div>
        </div>
    )
}