import React, { useState, useEffect } from 'react';
import ItemService from '../services/itemService';
import ItemCard from './ItemCard';
import { useNavigate } from 'react-router-dom';

const SearchItem = () => {
    const navigate = useNavigate();
    const [query, setQuery] = useState('');
    const [statusFilter, setStatusFilter] = useState('ALL'); // ALL, LOST, FOUND
    const [locationInput, setLocationInput] = useState('');
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        handleSearch();
    }, []);

    const handleSearch = async (e) => {
        if (e) e.preventDefault();
        setLoading(true);
        setError('');

        try {
            let response;
            if (query.trim() !== '') {
                response = await ItemService.searchItems(query);
                let fetched = response.data;
                
                if (statusFilter !== 'ALL') {
                    const checkFound = statusFilter === 'FOUND';
                    fetched = fetched.filter(item => item.found === checkFound);
                }
                if (locationInput.trim() !== '') {
                    fetched = fetched.filter(item => 
                        item.location.toLowerCase().includes(locationInput.toLowerCase())
                    );
                }
                setItems(fetched);
            } else if (locationInput.trim() !== '') {

                const isFound = statusFilter === 'FOUND';
                if (statusFilter === 'ALL') {
                    const res = await ItemService.getAllItems(0, 100);
                    let fetched = res.data.content !== undefined ? res.data.content : (res.data || []);
                    
                    fetched = fetched.filter(item => 
                        item.location.toLowerCase().includes(locationInput.toLowerCase())
                    );
                    setItems(fetched);
                } else {
                    const res = await ItemService.getAllItems(0, 100);
                    let fetched = res.data.content !== undefined ? res.data.content : (res.data || []);
                    const checkFound = statusFilter === 'FOUND';
                    fetched = fetched.filter(item => 
                        item.found === checkFound && 
                        item.location.toLowerCase().includes(locationInput.toLowerCase())
                    );
                    setItems(fetched);
                }
            } else {
                const res = await ItemService.getAllItems(0, 100);
                let fetched = res.data.content !== undefined ? res.data.content : (res.data || []);
                
                if (statusFilter !== 'ALL') {
                    const checkFound = statusFilter === 'FOUND';
                    fetched = fetched.filter(item => item.found === checkFound);
                }
                setItems(fetched);
            }
        } catch (err) {
            console.error("Search failed:", err);
            setError("Could not complete the search. Please make sure the backend is connected.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const delayDebounceFn = setTimeout(() => {
            handleSearch();
        }, 300);

        return () => clearTimeout(delayDebounceFn);
    }, [query, statusFilter, locationInput]);

    return (
        <div className="search-container">
            <div className="search-header">
                <h2>Advanced Search & Filter</h2>
                <p>Find reported items in real-time by keyword, location, and status.</p>
                
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
                {loading && <div className="loading-message">Searching items...</div>}
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