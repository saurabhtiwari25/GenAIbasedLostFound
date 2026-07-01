import React, { useState, useEffect, useCallback } from 'react';
import ItemService from '../services/itemService';
import ItemCard from './ItemCard';
import { useNavigate } from 'react-router-dom';
import Spinner from './Spinner';

const SearchItem = () => {
    const navigate = useNavigate();
    const [query, setQuery] = useState('');
    const [statusFilter, setStatusFilter] = useState('ALL'); // ALL, LOST, FOUND
    const [locationInput, setLocationInput] = useState('');
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');


    const handleSearch = useCallback(async (e) => {
        if (e) e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const res = query.trim()
                ? await ItemService.searchItems(query)
                : await ItemService.getAllItems();
            
            let fetched = res.data?.content || res.data || [];
            
            if (statusFilter !== 'ALL') {
                fetched = fetched.filter(i => i.found === (statusFilter === 'FOUND'));
            }
            if (locationInput.trim()) {
                fetched = fetched.filter(i =>
                    i.location.toLowerCase().includes(locationInput.toLowerCase())
                );
            }
            setItems(fetched);
        } catch (err) {
            console.error("Search failed:", err);
            setError("Could not complete the search. Please make sure the backend is connected.");
        } finally {
            setLoading(false);
        }
    }, [query, statusFilter, locationInput]);

    useEffect(() => {
        const delayDebounceFn = setTimeout(() => {
            handleSearch();
        }, 300);

        return () => clearTimeout(delayDebounceFn);
    }, [handleSearch]);

    return (
        <div className="search-container">
            <div className="search-header">
                <h2>Advanced Search & Filter</h2>
                <p>Find reported items in real-time by keyword, location and status.</p>
                
                <form onSubmit={handleSearch} className="search-form-card">
                    <div className="search-form-grid">
                        
                        <div className="input-group">
                            <label>Item Title / Keyword</label>
                            <div className="search-input-wrap">
                                <input
                                    type="text"
                                    placeholder="Search e.g. Keys, Wallet..."
                                    value={query}
                                    onChange={(e) => setQuery(e.target.value)}
                                />
                                <span className="icon-prefix">🔍</span>
                            </div>
                        </div>

                        <div className="input-group">
                            <label>Location</label>
                            <div className="search-input-wrap">
                                <input
                                    type="text"
                                    placeholder="e.g. Library, Cafe..."
                                    value={locationInput}
                                    onChange={(e) => setLocationInput(e.target.value)}
                                />
                                <span className="icon-prefix">📍</span>
                            </div>
                        </div>

                        <div className="input-group">
                            <label>Status</label>
                            <select 
                                value={statusFilter} 
                                onChange={(e) => setStatusFilter(e.target.value)}
                            >
                                <option value="ALL">All</option>
                                <option value="LOST"> Lost Items</option>
                                <option value="FOUND"> Found Items</option>
                            </select>
                        </div>
                    </div>
                </form>
            </div>

            <div className="search-results">
                {loading && <Spinner message="Searching items..." />}
                {error && <div className="search-error">{error}</div>}
                
                {!loading && items.length === 0 && (
                    <div className="search-empty-state">
                        <span className="icon">🔍</span>
                        <strong>No items found matching your filters.</strong>
                        <p>Try adjusting your keyword or location search.</p>
                    </div>
                )}

                {!loading && items.length > 0 && (
                    <div className="card-grid">
                        {items.map(item => (
                            <ItemCard 
                                key={item.id} 
                                item={item} 
                                onClick={() => navigate(`/item/${item.id}`)}
                            />
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default SearchItem;